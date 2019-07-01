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
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_ELECTRICITY;
import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class MeteringMeasurementMessageConsumerMeterReplacementTest extends MessageConsumerTest {

  private static final String QUANTITY = "Energy";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final String ADDRESS = "firstMeter";
  private static final String SECOND_METER_ADDRESS = "secondMeter";
  private static final String THIRD_METER_ADDRESS = "thirdMeter";

  private static final String EXTERNAL_ID = "ABC-123";
  private static final LocalDateTime MEASUREMENT_TIMESTAMP = LocalDateTime.parse(
    "2018-03-07T16:13:09");
  private static final ZonedDateTime ZONED_MEASUREMENT_TIMESTAMP = ZonedDateTime.of(
    MEASUREMENT_TIMESTAMP,
    METERING_TIMEZONE
  );

  private MeasurementMessageConsumer messageConsumer;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    messageConsumer = new MeteringMeasurementMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      new MeasurementUseCases(authenticatedUser, measurements, logicalMeters),
      gatewayUseCases,
      meterDefinitionUseCases,
      unitConverter,
      mediumProvider
    );
  }

  @Test
  public void periodIsSetForNewMeter() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );

    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

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

    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );

    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

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
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(5))
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP.minusDays(5),
          ZONED_MEASUREMENT_TIMESTAMP
        )
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP, null)
      ));
  }

  @Test
  public void activePeriodRemainsForLaterMeasurementWithinPeriod() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2))
    );
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1))
    );

    assertThat(physicalMeters.findAll()).hasSize(2);
    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        SECOND_METER_ADDRESS,
        PeriodRange.halfOpenFrom(ZONED_MEASUREMENT_TIMESTAMP.plusDays(2), null)
      ));

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> !p.isActive(now()))
      .hasSize(1)
      .extracting(p -> p.externalId, p -> p.address, p -> p.activePeriod)
      .contains(tuple(
        EXTERNAL_ID,
        ADDRESS,
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(2)
        )
      ));
  }

  @Test
  public void updateActivePeriodForEarlierMeasurement() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(2))
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(1))
    );

    assertThat(physicalMeters.findAll())
      .hasSize(2)
      .filteredOn(p -> p.address.equals(ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP.minusDays(2),
          ZONED_MEASUREMENT_TIMESTAMP.minusDays(1)
        )
      );

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(SECOND_METER_ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.from(ZONED_MEASUREMENT_TIMESTAMP.minusDays(1))
      );
  }

  @Test
  public void updateActivePeriodForEarlierMeasurement_ignorePreviousMetersMeasurement() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(2), MEASUREMENT_TIMESTAMP.minusDays(1))
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.minusDays(1))
    );

    // Measurement at minusDays(1) for this meter will now be outside active period
    assertThat(physicalMeters.findAll())
      .hasSize(2)
      .filteredOn(p -> p.address.equals(ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP.minusDays(2),
          ZONED_MEASUREMENT_TIMESTAMP.minusDays(1)
        )
      );

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(SECOND_METER_ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.from(ZONED_MEASUREMENT_TIMESTAMP.minusDays(1))
      );
  }

  @Test
  public void activePeriodRemainsForLaterMeasurementOutsidePeriod() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1))
    );
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2))
    );

    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .hasSize(2)
      .filteredOn(p -> p.address.equals(ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1)
        )
      );

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(SECOND_METER_ADDRESS))
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactlyInAnyOrder(
        PeriodRange.from(ZONED_MEASUREMENT_TIMESTAMP.plusDays(1))
      );
  }

  @Test
  public void activePeriodUpdatedForMeterReplacementNotInSequence() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );
    message(measurement()
      .meterId(THIRD_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(2))
    );
    message(measurement()
      .meterId(SECOND_METER_ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP.plusDays(1))
    );

    assertThat(physicalMeters.findAll()).hasSize(3);
    assertThat(logicalMeters.findAllBy(new MockRequestParameters())).hasSize(1);

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(ADDRESS))
      .extracting(p -> p.activePeriod)
      .containsExactly(
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP,
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1)
        )
      );

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(THIRD_METER_ADDRESS))
      .extracting(p -> p.activePeriod)
      .containsExactly(
        PeriodRange.from(
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(2)
        )
      );

    assertThat(physicalMeters.findAll())
      .filteredOn(p -> p.address.equals(SECOND_METER_ADDRESS))
      .extracting(p -> p.activePeriod)
      .containsExactly(
        PeriodRange.halfOpenFrom(
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(1),
          ZONED_MEASUREMENT_TIMESTAMP.plusDays(2)
        )
      );
  }

  @Test
  public void ignoresMeasurementFromMeterWithInvalidClock() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(MEASUREMENT_TIMESTAMP)
    );

    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(LocalDateTime.parse("2000-01-01T02:00:00"))
    );

    assertThat(physicalMeters.findAll())
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactly(PeriodRange.from(ZONED_MEASUREMENT_TIMESTAMP));

    assertThat(measurements.allMocks())
      .extracting(m -> m.readoutTime)
      .containsExactly(ZONED_MEASUREMENT_TIMESTAMP);
  }

  @Test
  public void ignoresMeasurementFromMeterWithInvalidClock_createsMeter() {
    message(measurement()
      .meterId(ADDRESS)
      .valuesAtTimestamps(LocalDateTime.parse("2000-01-01T02:00:00"))
    );

    assertThat(physicalMeters.findAll())
      .hasSize(1)
      .extracting(p -> p.activePeriod)
      .containsExactly(PeriodRange.empty());

    assertThat(measurements.allMocks()).hasSize(0);
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
    physicalMeters.save(
      PhysicalMeter.builder()
        .address(ADDRESS)
        .externalId(EXTERNAL_ID)
        .manufacturer("ELV")
        .medium(Medium.UNKNOWN_MEDIUM)
        .readIntervalMinutes(15)
        .organisationId(organisation.id)
        .medium(DEFAULT_ELECTRICITY.medium.name)
        .logicalMeterId(logicalMeter.id)
        .activePeriod(PeriodRange.empty())
        .build());
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.saveAndFlush(ORGANISATION);
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

    private String meterId = ADDRESS;

    private List<ValueDto> values = List.of(new ValueDto(
      MEASUREMENT_TIMESTAMP,
      1.0,
      "kWh",
      QUANTITY
    ));

    private MeteringMeasurementMessageDtoBuilder meterId(String meterId) {
      this.meterId = meterId;
      return this;
    }

    private MeteringMeasurementMessageDtoBuilder valuesAtTimestamps(LocalDateTime... timestamps) {
      this.values = Arrays.stream(timestamps)
        .map(ts -> new ValueDto(ts, 1.0, "kWh", QUANTITY))
        .collect(toList());
      return this;
    }

    private MeteringMeasurementMessageDto build() {
      return new MeteringMeasurementMessageDto(
        new IdDto(GATEWAY_EXTERNAL_ID),
        new IdDto(meterId),
        new IdDto(EXTERNAL_ID),
        ORGANISATION_EXTERNAL_ID,
        "Elvaco Metering",
        values
      );
    }
  }
}
