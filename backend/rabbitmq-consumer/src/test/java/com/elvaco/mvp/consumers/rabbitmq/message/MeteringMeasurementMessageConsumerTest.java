package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.repository.MockMeasurements;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_HOT_WATER;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MeteringMeasurementMessageConsumerTest extends MessageConsumerTest {

  private static final String QUANTITY = "Energy";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final String ADDRESS = "1234";
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

  private MeasurementMessageConsumer messageConsumer;
  private MockMeasurements measurements;

  @Before
  public void setUp() {
    super.setUp();
    measurements = new MockMeasurements();

    messageConsumer = new MeteringMeasurementMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      new MeasurementUseCases(authenticatedUser, measurements),
      gatewayUseCases,
      meterDefinitionUseCases,
      unitConverter,
      mediumProvider
    );
  }

  @Test
  public void survivesMissingGatewayFieldInMeasurementMessage() {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      null,
      new MeterIdDto(EXTERNAL_ID),
      new FacilityIdDto(EXTERNAL_ID),
      ORGANISATION_SLUG,
      "Test source system",
      List.of(new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "kWh", QUANTITY))
    );

    messageConsumer.accept(message);

    assertThat(gateways.findAll()).isEmpty();
    assertThat(organisations.findAll()).hasSize(1);
    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void survivesEmptyGatewayFieldInMeasurementMessage() {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      new GatewayIdDto(null),
      new MeterIdDto(EXTERNAL_ID),
      new FacilityIdDto(EXTERNAL_ID),
      ORGANISATION_SLUG,
      "Test source system",
      List.of(new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "kWh", QUANTITY))
    );

    messageConsumer.accept(message);

    assertThat(gateways.findAll()).isEmpty();
    assertThat(organisations.findAll()).hasSize(1);
    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void measurementIsUpdated() {
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(LogicalMeter.builder()
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .build());

    messageConsumer.accept(newMeasurementMessage(new ValueDto(
      MEASUREMENT_TIMESTAMP,
      1.0,
      "W",
      "Power"
    )));
    messageConsumer.accept(newMeasurementMessage(new ValueDto(
      MEASUREMENT_TIMESTAMP,
      2.0,
      "W",
      "Power"
    )));

    List<Measurement> actual = measurements.allMocks();
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).value).isEqualTo(2.0);
  }

  @Test
  public void measurementIsMappedToMvpMeasurements() {
    Organisation organisation = saveDefaultOrganisation();

    logicalMeters.save(LogicalMeter.builder()
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .build());

    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      new GatewayIdDto(GATEWAY_EXTERNAL_ID),
      new MeterIdDto(ADDRESS),
      new FacilityIdDto(EXTERNAL_ID),
      organisation.externalId,
      "Elvaco Metering",
      asList(
        new ValueDto(
          LocalDateTime.parse("2018-03-16T13:07:01"),
          35.0,
          "°C",
          "Return temp."
        ),
        new ValueDto(
          LocalDateTime.parse("2018-03-16T14:07:01"),
          36.7,
          "°C",
          "Return temp."
        )
      )
    );

    messageConsumer.accept(message);

    List<Measurement> allMeasurements = measurements.allMocks();
    assertThat(allMeasurements).hasSize(2);
    assertThat(allMeasurements).allMatch(measurement ->
      measurement.quantity.equals("Return temperature") && measurement.unit.equals("°C")
    );
  }

  @Test
  public void measurementIsAcceptedForDifferentQuantitiesWithSameTimestamp() {
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(LogicalMeter.builder()
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .build());

    messageConsumer.accept(newMeasurementMessage(new ValueDto(
      MEASUREMENT_TIMESTAMP,
      1.0,
      "W",
      "Power"
    )));
    messageConsumer.accept(newMeasurementMessage(new ValueDto(
      MEASUREMENT_TIMESTAMP,
      2.0,
      "°C",
      "Flow temp."
    )));

    assertThat(measurements.allMocks()).hasSize(2);
  }

  @Test
  public void unknownQuantityIsDiscarded_knownQuantitiesAreRetained() {
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .build());

    messageConsumer.accept(newMeasurementMessage(
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        1.0,
        "W",
        "Half Life 3"
      ),
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        1.0,
        "kWh",
        "Energy"
      )
    ));
    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0).quantity).isEqualTo("Energy");
  }

  @Test
  public void invalidUnitForQuantityIsDiscarded_validUnitsAreRetained() {
    var organisation = saveDefaultOrganisation();
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .build());

    messageConsumer.accept(newMeasurementMessage(
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        3.0,
        "kWh",
        "External temperature"
      ),
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        1.0,
        "kWh",
        "Energy"
      )
    ));
    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).extracting(m -> m.unit).containsExactly("kWh");
    assertThat(createdMeasurements).extracting(m -> m.quantity).containsExactly("Energy");
  }

  @Test
  public void differentUnitOfSameDimensionIsAccepted() {
    var organisation = saveDefaultOrganisation();
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .build());

    messageConsumer.accept(newMeasurementMessage(
      new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "K", "External temp"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 2.0, "m³/s", "Volume flow"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 3.0, "Wh", "Energy"),
      new ValueDto(MEASUREMENT_TIMESTAMP.plusHours(1), 4.0, "MWh", "Energy"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 5.0, "kW", "Power"),
      new ValueDto(MEASUREMENT_TIMESTAMP.plusHours(1), 6.0, "MW", "Power"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 7.0, "°C", "Difference temp.")
    ));

    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).extracting(m -> m.value).containsExactlyInAnyOrder(
      1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0
    );
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    Organisation organisation = saveDefaultOrganisation();

    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      physicalMeter()
        .organisationId(organisation.id)
        .medium("Electricity")
        .build()
    );

    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    Measurement expectedMeasurement = Measurement.builder()
      .created(ZONED_MEASUREMENT_TIMESTAMP)
      .quantity(QUANTITY)
      .value(1.0)
      .unit("kWh")
      .physicalMeter(expectedPhysicalMeter)
      .build();
    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  @Test
  public void createsLogicalMeterForMeasurement() {
    Organisation organisation = saveDefaultOrganisation();

    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    )).isNotEmpty();
  }

  @Test
  public void createsOrganisationAndMeterForMeasurement() {
    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    Organisation organisation = organisations.findBySlug(ORGANISATION_SLUG).get();

    PhysicalMeter physicalMeter = physicalMeters.findBy(
      organisation.id,
      EXTERNAL_ID,
      ADDRESS
    ).get();
    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get();

    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(Measurement.builder()
      .created(ZONED_MEASUREMENT_TIMESTAMP)
      .quantity(QUANTITY)
      .value(1.0)
      .unit("kWh")
      .physicalMeter(PhysicalMeter.builder()
        .id(physicalMeter.id)
        .organisationId(organisation.id)
        .address(ADDRESS)
        .externalId(EXTERNAL_ID)
        .medium(Medium.UNKNOWN_MEDIUM)
        .logicalMeterId(logicalMeter.id)
        .readIntervalMinutes(60)
        .activePeriod(physicalMeter.activePeriod)
        .build()
      ).build());
  }

  @Test
  public void setOrganisationDefaultMeterDefinitionForDistrictHeating() {
    Organisation organisation = saveDefaultOrganisation();

    meterDefinitions.save(MeterDefinition.builder()
      .organisation(organisation)
      .medium(new Medium(0L, Medium.DISTRICT_HEATING))
      .name("OrganisationDefault")
      .autoApply(true)
      .build());

    messageConsumer.accept(newMeasurementMessage(getDistrictHeatingValues()));

    assertThat(logicalMeters.findAllByOrganisationId(organisation.id))
      .flatExtracting(lm -> lm.meterDefinition.name, lm -> lm.meterDefinition.isDefault())
      .containsOnly("OrganisationDefault", false);
  }

  @Test
  public void usesOrganisationExternalIdForMeasurementMessage() {
    saveDefaultOrganisation();

    messageConsumer.accept(newMeasurementMessage(ORGANISATION_EXTERNAL_ID));

    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements.get(0).physicalMeter.organisationId).isEqualTo(ORGANISATION.id);
  }

  @Test
  public void gatewayIsCreatedFromMeasurementMessage() {
    MeteringMeasurementMessageDto message = measurementMessageWithUnit("kWh");

    messageConsumer.accept(message);

    assertThat(gateways.findAll()).hasSize(1);
  }

  @Test
  public void measurementUnitIsUpdated() {
    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    List<Measurement> all = measurements.allMocks();

    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("kWh");
    assertThat(all.get(0).value).isEqualTo(1.0);

    messageConsumer.accept(measurementMessageWithUnit("MWh"));

    all = measurements.allMocks();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("MWh");
    assertThat(all.get(0).value).isEqualTo(1.0);
  }

  @Test
  public void measurementValueIsUpdated() {
    messageConsumer.accept(newMeasurementMessage(1.0));

    List<Measurement> all = measurements.allMocks();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).value).isEqualTo(1.0);

    messageConsumer.accept(newMeasurementMessage(2.0));

    all = measurements.allMocks();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).value).isEqualTo(2.0);
  }

  @Test
  public void measurementTimezoneOffsetAtNormalTime() {
    ValueDto valueAtNormalTime = new ValueDto(
      LocalDateTime.parse("2018-01-07T16:13:09"),
      1.0,
      "kWh",
      QUANTITY
    );

    messageConsumer.accept(newMeasurementMessage(valueAtNormalTime));

    List<Measurement> all = measurements.allMocks();
    assertThat(all.get(0).created.toOffsetDateTime().getOffset()).isEqualTo(ZoneOffset.ofHours(1));
  }

  @Test
  public void measurementTimezoneOffsetAtDaylightSavingTime() {
    ValueDto valueAtDaylightSavingTime = new ValueDto(
      LocalDateTime.parse("2018-07-07T14:32:27"),
      1.0,
      "kWh",
      QUANTITY
    );

    messageConsumer.accept(newMeasurementMessage(valueAtDaylightSavingTime));

    List<Measurement> all = measurements.allMocks();
    assertThat(all.get(0).created.toOffsetDateTime().getOffset()).isEqualTo(ZoneOffset.ofHours(1));
  }

  @Test
  public void measurementValue_WithOnTimeQuantityIsIgnored() {
    messageConsumer.accept(newMeasurementMessage(
      new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "W", "Power"),
      new ValueDto(MEASUREMENT_TIMESTAMP.plusHours(1), 2.0, "W", "Power"),
      new ValueDto(MEASUREMENT_TIMESTAMP.plusHours(2), 1235.0, "h", "On time"),
      new ValueDto(MEASUREMENT_TIMESTAMP.plusHours(3), 3.0, "W", "Power")
    ));

    assertThat(measurements.allMocks()).extracting("value")
      .containsExactlyInAnyOrder(1.0, 2.0, 3.0);
  }

  @Test
  public void measurementValueFor_MissingLogicalMeter_CreatesNewLogicalMeter() {
    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.facility.id).isEqualTo("ABC-123");
    assertThat(response.meter.id).isEqualTo("1234");
  }

  @Test
  public void measurementValueFor_MissingPhysicalMeter_CreatesNewPhysicalMeter() {
    Organisation organisation = saveDefaultOrganisation();

    logicalMeters.save(LogicalMeter.builder()
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .meterDefinition(DEFAULT_HOT_WATER)
      .build());

    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.facility.id).isEqualTo("ABC-123");
    assertThat(response.meter.id).isEqualTo("1234");
    assertThat(response.gateway.id).isEqualTo("123");
  }

  @Test
  public void measurementValueFor_ExistingGateway_DoesNotCreateNewGateway() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));

    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.gateway).isNull();
    assertThat(response.facility).isNotNull();
    assertThat(response.meter).isNotNull();
  }

  @Test
  public void measurementValueFor_ExistingGateway_DoesNotModifyGateway() {
    Organisation organisation = saveDefaultOrganisation();
    Gateway existingGateway = gateways.save(newGateway(organisation.id));

    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    List<Gateway> all = gateways.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0)).isEqualTo(existingGateway);
  }

  @Test
  public void measurementValueFor_ExistingEntities_CreateNoNewEntities() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    physicalMeters.save(physicalMeter()
      .organisationId(organisation.id)
      .medium("Hot water")
      .revision(1)
      .build());
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_HOT_WATER)
        .location(kungsbacka().build())
        .build()
    );

    Optional<GetReferenceInfoDto> response =
      messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(response.isPresent()).isFalse();
  }

  @Test
  public void measurementValueFor_ExistingEntities_CreateNewPhysicalMeter() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    physicalMeters.save(physicalMeter().organisationId(organisation.id).build());
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_HOT_WATER)
        .location(kungsbacka().build())
        .build()
    );

    Optional<GetReferenceInfoDto> response =
      messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(response.isPresent()).isTrue();
    assertThat(response.get().meter.id).isEqualTo(ADDRESS);
    assertThat(response.get().facility.id).isEqualTo(EXTERNAL_ID);
  }

  @Test
  public void measurementValueFor_ExistingEntities_CreateNewLogicalMeter() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    physicalMeters.save(physicalMeter()
      .organisationId(organisation.id)
      .medium("Hot water")
      .build());
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_HOT_WATER)
        .build()
    );

    Optional<GetReferenceInfoDto> response =
      messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(response.isPresent()).isTrue();
    assertThat(response.get().meter.id).isEqualTo(ADDRESS);
    assertThat(response.get().facility.id).isEqualTo(EXTERNAL_ID);
  }

  @Test
  public void measurementValueFor_ExistingEntities_CreateNewGateway() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(Gateway.builder()
      .organisationId(organisation.id)
      .serial(GATEWAY_EXTERNAL_ID)
      .productModel("")
      .build());

    physicalMeters.save(physicalMeter()
      .organisationId(organisation.id)
      .medium("Hot water")
      .build());
    logicalMeters.save(
      LogicalMeter.builder()
        .externalId(EXTERNAL_ID)
        .organisationId(organisation.id)
        .meterDefinition(DEFAULT_HOT_WATER)
        .location(kungsbacka().build())
        .build()
    );

    Optional<GetReferenceInfoDto> response =
      messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(response.isPresent()).isTrue();
    assertThat(response.get().gateway.id).isEqualTo(GATEWAY_EXTERNAL_ID);
    assertThat(response.get().facility.id).isEqualTo(EXTERNAL_ID);
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(ORGANISATION);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(double value) {
    return newMeasurementMessage(new ValueDto(MEASUREMENT_TIMESTAMP, value, "kWh", QUANTITY));
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(String organisationExternalId) {
    return newMeasurementMessage(
      organisationExternalId,
      new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "kWh", QUANTITY)
    );
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(ValueDto... valueDto) {
    return newMeasurementMessage(ORGANISATION_EXTERNAL_ID, valueDto);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(
    String organisationExternalId,
    ValueDto... valueDto
  ) {
    return new MeteringMeasurementMessageDto(
      new GatewayIdDto(GATEWAY_EXTERNAL_ID),
      new MeterIdDto(ADDRESS),
      new FacilityIdDto(EXTERNAL_ID),
      organisationExternalId,
      "Elvaco Metering",
      asList(valueDto)
    );
  }

  private MeteringMeasurementMessageDto measurementMessageWithUnit(String unit) {
    return newMeasurementMessage(new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, unit, QUANTITY));
  }

  private Gateway newGateway(UUID organisationId) {
    return Gateway.builder()
      .organisationId(organisationId)
      .serial(GATEWAY_EXTERNAL_ID)
      .productModel("CMi2110")
      .build();
  }

  private ValueDto[] getDistrictHeatingValues() {
    return List.of(
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        1.0,
        Quantity.RETURN_TEMPERATURE.storageUnit,
        "Return temp."
      ),
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        2.0,
        Quantity.DIFFERENCE_TEMPERATURE.storageUnit,
        "Difference temp."
      ),
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        3.0,
        Quantity.FORWARD_TEMPERATURE.storageUnit,
        "Flow temp."
      ),
      new ValueDto(
        MEASUREMENT_TIMESTAMP,
        4.0,
        Quantity.VOLUME_FLOW.storageUnit,
        "Volume flow"
      ),
      new ValueDto(MEASUREMENT_TIMESTAMP, 5.0, Quantity.POWER.storageUnit, "Power"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 6.0, Quantity.VOLUME.storageUnit, "Volume"),
      new ValueDto(MEASUREMENT_TIMESTAMP, 7.0, Quantity.ENERGY.storageUnit, "Energy")
    ).toArray(new ValueDto[6]);
  }

  private static PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .address(ADDRESS)
      .externalId(EXTERNAL_ID)
      .manufacturer("ELV")
      .medium(Medium.UNKNOWN_MEDIUM)
      .readIntervalMinutes(15);
  }
}
