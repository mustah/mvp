package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockMeterStatusLogs;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_ELECTRICITY;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class MeteringMeasurementMessageConsumerMeterReplacementTest {

  private static final String QUANTITY = "Energy";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final String ADDRESS = "1234";
  private static final String SECOND_METER_ADDRESS = "9876";
  private static final String ORGANISATION_EXTERNAL_ID = "Some Organisation";
  private static final String ORGANISATION_SLUG = "some-organisation";

  private static final Organisation ORGANISATION = new Organisation(
    randomUUID(),
    ORGANISATION_EXTERNAL_ID,
    ORGANISATION_SLUG,
    ORGANISATION_EXTERNAL_ID
  );

  private static final String EXTERNAL_ID = "ABC-123";
  private static final LocalDateTime MEASUREMENT_TIMESTAMP = LocalDateTime.parse(
    "2018-03-07T16:13:09");
  private static final ZonedDateTime ZONED_MEASUREMENT_TIMESTAMP = ZonedDateTime.of(
    MEASUREMENT_TIMESTAMP,
    METERING_TIMEZONE
  );

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Gateways gateways;
  private MeasurementMessageConsumer messageConsumer;

  @Before
  public void setUp() {
    User superAdmin = new UserBuilder()
      .name("super-admin")
      .email("super@admin.io")
      .password("password")
      .organisationElvaco()
      .asSuperAdmin()
      .build();
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .organisation(ORGANISATION)
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    physicalMeters = new MockPhysicalMeters();
    organisations = new MockOrganisations();
    MockMeasurements measurements = new MockMeasurements();
    logicalMeters = new MockLogicalMeters();
    gateways = new MockGateways();

    messageConsumer = new MeteringMeasurementMessageConsumer(
      new LogicalMeterUseCases(authenticatedUser, logicalMeters),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters, new MockMeterStatusLogs()),
      new OrganisationUseCases(
        authenticatedUser,
        organisations,
        new OrganisationPermissions(new MockUsers(singletonList(superAdmin)))
      ),
      new MeasurementUseCases(authenticatedUser, measurements),
      new GatewayUseCases(gateways, authenticatedUser),
      new UnitConverter() {
        @Override
        public MeasurementUnit convert(
          MeasurementUnit measurementUnit, String targetUnit
        ) {
          return null;
        }

        @Override
        public boolean isSameDimension(String firstUnit, String secondUnit) {
          return true;
        }
      }
    );
  }

  @Test
  public void periodIsSetForNewMeter() {
    message(measurement());

    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP, null)
      ));
  }

  @Test
  public void periodIsSetForExistingMeter() {
    givenLogicalAndPhysical();

    message(measurement());

    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP, null)
      ));
  }

  @Test
  public void activeMeterIsReplacedWithNewMeterOfSameFacility() {
    ZonedDateTime firstMeterPeriodStart = ZONED_MEASUREMENT_TIMESTAMP.minusDays(5);
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(firstMeterPeriodStart);

    message(measurement().meterId(SECOND_METER_ADDRESS));

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(firstMeterPeriodStart, ZONED_MEASUREMENT_TIMESTAMP)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP, null)
      ));
  }

  @Test
  public void periodIsNotUpdatedForActiveMeterWhenMeasurementForSameMeterIsOlder() {
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(ZONED_MEASUREMENT_TIMESTAMP);

    message(measurement().meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2)));
    message(measurement().meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1)));

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(2), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(2)
        )
      ));
  }

  @Test
  public void periodIsNotUpdatedForPassiveMeterWhenMeasurementForSameMeterIsOlder() {
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(ZONED_MEASUREMENT_TIMESTAMP);

    message(measurement().meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1)));

    message(measurement().valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(1)));

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(1), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1)
        )
      ));
  }

  @Test
  public void periodIsNotUpdatedForPassiveMeterWhenMeasurementWithinPeriod() {
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(ZONED_MEASUREMENT_TIMESTAMP);

    message(measurement().meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2)));

    message(measurement().valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1)));

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(2), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(2)
        )
      ));
  }

  @Test
  public void periodIsNotUpdatedWhenMeasurementForActiveMeterIsWithinPassiveMeterPeriod() {
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(ZONED_MEASUREMENT_TIMESTAMP);

    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1))
    );

    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(1))
    );

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(1), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1)
        )
      ));
  }

  @Test
  public void periodIsNotUpdatedWhenMeasurementForPassiveMeterIsWithinActiveMeterPeriod() {
    var logicalMeter = givenLogicalAndPhysicalAndMeasurement(ZONED_MEASUREMENT_TIMESTAMP);
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1))
    );

    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2))
    );

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(1), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.logicalMeterId, p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        logicalMeter.id,
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1)
        )
      ));
  }

  @Test
  public void getEarliestMeasurementTimestamp() {
    var message = measurement()
      .valuesAtTimestamps(
        MEASUREMENT_TIMESTAMP,
        MEASUREMENT_TIMESTAMP.minusHours(1),
        MEASUREMENT_TIMESTAMP.plusHours(1)
      )
      .build();

    assertThat(((MeteringMeasurementMessageConsumer) messageConsumer).getEarliestTimestamp(message))
      .isEqualTo(MEASUREMENT_TIMESTAMP.minusHours(1).atZone(METERING_TIMEZONE));
  }

  @Test
  public void getEarliestMeasurementTimestamp_throwsExceptionWhenMissingTimestamp() {
    var message = measurement().valuesAtTimestamps().build();

    assertThatThrownBy(() -> ((MeteringMeasurementMessageConsumer) messageConsumer)
      .getEarliestTimestamp(message))
      .isInstanceOf(IllegalArgumentException.class);
  }

  private MeteringMeasurementMessageDtoBuilder measurement() {
    return new MeteringMeasurementMessageDtoBuilder();
  }

  private void givenLogicalAndPhysical() {
    var organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    var logicalMeter = logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_ELECTRICITY)
        .build());
    var physicalMeter = physicalMeters.save(
      PhysicalMeter.builder()
        .address(ADDRESS)
        .externalId(EXTERNAL_ID)
        .manufacturer("ELV")
        .medium(Medium.UNKNOWN_MEDIUM.name)
        .readIntervalMinutes(15)
        .organisationId(organisation.id)
        .medium(DEFAULT_ELECTRICITY.medium.name)
        .logicalMeterId(logicalMeter.id)
        .activePeriod(PeriodRange.empty())
        .build());
    logicalMeters.save(logicalMeter.addPhysicalMeter(physicalMeter));
  }

  private LogicalMeter givenLogicalAndPhysicalAndMeasurement(ZonedDateTime activePeriodStart) {
    var organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    var logicalMeter = logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_ELECTRICITY)
        .build());
    var physicalMeter = physicalMeters.save(
      PhysicalMeter.builder()
        .address(ADDRESS)
        .externalId(EXTERNAL_ID)
        .manufacturer("ELV")
        .medium(Medium.UNKNOWN_MEDIUM.name)
        .readIntervalMinutes(15)
        .organisationId(organisation.id)
        .medium(DEFAULT_ELECTRICITY.medium.name)
        .logicalMeterId(logicalMeter.id)
        .activePeriod(PeriodRange.from(PeriodBound.inclusiveOf(activePeriodStart)))
        .build());
    logicalMeters.save(logicalMeter.addPhysicalMeter(physicalMeter));
    message(measurement().valuesAtTimestamps(activePeriodStart.toLocalDateTime()));
    return logicalMeter;
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(ORGANISATION);
  }

  private Gateway newGateway(UUID organisationId) {
    return Gateway.builder()
      .organisationId(organisationId)
      .serial(GATEWAY_EXTERNAL_ID)
      .productModel("CMi2110")
      .build();
  }

  private void message(MeteringMeasurementMessageDtoBuilder builder) {
    messageConsumer.accept(builder.build());
  }

  private static class MeteringMeasurementMessageDtoBuilder {

    String gatewayId = GATEWAY_EXTERNAL_ID;
    String meterId = ADDRESS;
    String facilityId = EXTERNAL_ID;
    String organisationId = ORGANISATION_EXTERNAL_ID;
    String sourceSystemId = "Elvaco Metering";
    List<ValueDto> values = List.of(new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "kWh", QUANTITY));

    MeteringMeasurementMessageDtoBuilder meterId(String meterId) {
      this.meterId = meterId;
      return this;
    }

    MeteringMeasurementMessageDtoBuilder valuesAtTimestamps(LocalDateTime... timestamps) {
      this.values = Arrays.stream(timestamps)
        .map(ts -> new ValueDto(ts, 1.0, "kWh", QUANTITY))
        .collect(toList());
      return this;
    }

    MeteringMeasurementMessageDto build() {
      return new MeteringMeasurementMessageDto(
        new GatewayIdDto(gatewayId),
        new MeterIdDto(meterId),
        new FacilityIdDto(facilityId),
        organisationId,
        sourceSystemId,
        values
      );
    }
  }
}
