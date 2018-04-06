package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.geocode.MockGeocodeService;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MessageHandlerTest {

  public static final String DEFAULT_MANUFACTURER = "ELV";
  private static final String DEFAULT_PRODUCT_MODEL = "CMi2110";
  private static final String DEFAULT_QUANTITY = "Energy";
  private static final String DEFAULT_GATEWAY_EXTERNAL_ID = "123";
  private static final int DEFAULT_EXPECTED_INTERVAL = 15;
  private static final String DEFAULT_MEDIUM = "Hot water";
  private static final String DEFAULT_ADDRESS = "1234";
  private static final String DEFAULT_ORGANISATION_EXTERNAL_ID = "some-organisation";
  private static final String DEFAULT_EXTERNAL_ID = "ABC-123";
  private static final ZonedDateTime EXPECTED_DATETIME = ZonedDateTime.of(
    2018,
    3,
    7,
    16,
    13,
    9,
    0,
    ZoneId.of("CET")
  );
  private static final LocalDateTime MEASUREMENT_TIMESTAMP = LocalDateTime.parse(
    "2018-03-07T16:13:09");
  private static final String DEFAULT_UNIT = "kWh";
  private static final Location DEFAULT_LOCATION = new Location(
    "Sweden",
    "Kungsbacka",
    "Kabelgatan 2T"
  );
  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Gateways gateways;
  private Measurements measurements;
  private MeteringMessageHandler messageHandler;
  private MockGeocodeService geocodeService;

  @Before
  public void setUp() {
    physicalMeters = new MockPhysicalMeters();
    User superAdmin = new UserBuilder()
      .name("super-admin")
      .email("super@admin.io")
      .password("password")
      .organisationElvaco()
      .asSuperAdmin()
      .build();
    organisations = new MockOrganisations();
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .organisation(new Organisation(randomUUID(), "some organisation",
          DEFAULT_ORGANISATION_EXTERNAL_ID
        ))
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    OrganisationUseCases organisationUseCases = new OrganisationUseCases(
      authenticatedUser,
      organisations,
      new OrganisationPermissions(new MockUsers(singletonList(superAdmin)))
    );
    measurements = new MockMeasurements();
    logicalMeters = new MockLogicalMeters();
    gateways = new MockGateways();
    geocodeService = new MockGeocodeService();

    this.messageHandler = new MeteringMessageHandler(
      new LogicalMeterUseCases(
        authenticatedUser,
        logicalMeters,
        measurements
      ),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters),
      organisationUseCases,
      new MeasurementUseCases(authenticatedUser, this.measurements),
      new GatewayUseCases(gateways, authenticatedUser),
      geocodeService
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    Organisation organisation = findOrganisation();

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      DEFAULT_EXTERNAL_ID
    ).get();

    PhysicalMeter savedPhysicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      DEFAULT_EXTERNAL_ID,
      DEFAULT_ADDRESS
    ).get();

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      logicalMeter.id,
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      new Location("Sweden", "Kungsbacka", "Kabelgatan 2T"),
      logicalMeter.created,
      singletonList(savedPhysicalMeter),
      MeterDefinition.HOT_WATER_METER,
      singletonList(gateways.findBy(
        organisation.id,
        DEFAULT_PRODUCT_MODEL,
        DEFAULT_GATEWAY_EXTERNAL_ID
      ).get())
    );

    assertThat(logicalMeter).isEqualTo(expectedLogicalMeter);
    assertThat(savedPhysicalMeter).isEqualTo(new PhysicalMeter(
      savedPhysicalMeter.id,
      organisation,
      DEFAULT_ADDRESS,
      DEFAULT_EXTERNAL_ID,
      DEFAULT_MEDIUM,
      DEFAULT_MANUFACTURER,
      logicalMeter.id,
      DEFAULT_EXPECTED_INTERVAL,
      null,
      savedPhysicalMeter.statuses
    ));
  }

  @Test
  public void updatesExistingGatewayWithUnknownManufacturer() {
    UUID gatewayId = UUID.randomUUID();
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    gateways.save(new Gateway(
      gatewayId,
      organisation.id,
      DEFAULT_GATEWAY_EXTERNAL_ID,
      "Unknown"
    ));

    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    Gateway gateway = gateways.findBy(organisation.id, DEFAULT_GATEWAY_EXTERNAL_ID).get();
    assertThat(gateway.id).isEqualTo(gatewayId);
    assertThat(gateway.productModel).isEqualTo(DEFAULT_PRODUCT_MODEL);
  }

  @Test
  public void survivesMissingGatewayFieldInMeasurementMessage() {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      null,
      new MeterIdDto(DEFAULT_EXTERNAL_ID),
      new FacilityIdDto(DEFAULT_EXTERNAL_ID),
      DEFAULT_ORGANISATION_EXTERNAL_ID,
      "Test source system",
      emptyList()
    );

    messageHandler.handle(message);

    assertThat(gateways.findAll(null)).isEmpty();
    assertThat(organisations.findAll()).hasSize(1);
    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAll(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void locationIsUpdatedForExistingMeter() {
    UUID meterId = UUID.randomUUID();
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    logicalMeters.save(new LogicalMeter(
      meterId,
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now()

    ));

    Location newLocation = new Location("", "Växjö", "Gatvägen 41");
    messageHandler.handle(newStructureMessage(newLocation));

    assertThat(logicalMeters.findById(meterId).get().location).isEqualTo(newLocation);
  }

  @Test
  public void createsOrganisationWithSameNameAsCode() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    Organisation organisation = findOrganisation();

    assertThat(organisation.name).isEqualTo(DEFAULT_ORGANISATION_EXTERNAL_ID);
  }

  @Test
  public void createsMeterAndGatewayForExistingOrganisation() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    Organisation organisation = findOrganisation();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(logicalMeters.findAll(new MockRequestParameters())).hasSize(1);
    assertThat(organisations.findAll()).hasSize(1);
    PhysicalMeter meter = allPhysicalMeters.get(0);
    assertThat(meter.organisation).isEqualTo(organisation);

    LogicalMeter logicalMeter = logicalMeters.findById(meter.logicalMeterId).get();
    assertThat(logicalMeter.meterDefinition).isEqualTo(MeterDefinition.HOT_WATER_METER);
    assertThat(gateways.findBy(organisation.id, DEFAULT_PRODUCT_MODEL, DEFAULT_GATEWAY_EXTERNAL_ID)
      .isPresent()).isTrue();
  }

  @Test
  public void addsPhysicalMeterToExistingLogicalMeter() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    LogicalMeter saved = findLogicalMeter();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(saved.id);
  }

  @Test
  public void resendingSameMessageShouldNotUpdateExistingGateways() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    List<Gateway> allAfterFirstMessage = gateways.findAll(new MockRequestParameters());
    assertThat(allAfterFirstMessage).hasSize(1);

    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    assertThat(gateways.findAll(new MockRequestParameters())).isEqualTo(allAfterFirstMessage);
  }

  @Test
  public void gatewaysAreConnectedToMeters() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    List<Gateway> all = gateways.findAll(new MockRequestParameters());
    assertThat(all.stream().anyMatch(gateway -> gateway.meters.isEmpty())).isFalse();
  }

  @Ignore
  @Test
  public void newLogicalMeterIsConnectedToExistingGateway() {
    // TODO[!must!]
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    messageHandler.handle(newStructureMessage("Unmappable medium"));

    List<LogicalMeter> meters = logicalMeters.findAll(new MockRequestParameters());
    assertThat(meters).hasSize(1);
    assertThat(meters.get(0).getMedium()).isEqualTo("Unknown meter");
  }

  @Test
  public void callsGeocodeService() {
    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    LocationWithId expectedLocationWithId = new LocationBuilder()
      .country("Sweden")
      .city("Kungsbacka")
      .streetAddress("Kabelgatan 2T")
      .id(geocodeService.requestId)
      .buildLocationWithId();

    assertThat(geocodeService.requestId).isNotNull();
    assertThat(geocodeService.location).isEqualTo(expectedLocationWithId);
  }

  @Test
  public void addsSecondPhysicalMeterToExistingLogicalMeter() {
    Organisation organisation = organisations.save(
      newOrganisation("An existing organisation", DEFAULT_ORGANISATION_EXTERNAL_ID)
    );
    ZonedDateTime now = ZonedDateTime.now();
    UUID logicalMeterId = UUID.randomUUID();
    PhysicalMeter existingPhysicalMeter = physicalMeters.save(newPhysicalMeter(
      organisation,
      DEFAULT_MEDIUM
    ).withLogicalMeterId(logicalMeterId));

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      UNKNOWN_LOCATION,
      now,
      singletonList(existingPhysicalMeter),
      MeterDefinition.HOT_WATER_METER,
      emptyList()
    ));

    MeteringMeterStructureMessageDto structureMessage =
      newStructureMessage(DEFAULT_MEDIUM, "4321");

    messageHandler.handle(structureMessage);

    List<LogicalMeter> organisationMeters = logicalMeters.findByOrganisationId(organisation.id);
    assertThat(organisationMeters).hasSize(1);
    LogicalMeter meter = organisationMeters.get(0);
    assertThat(meter.physicalMeters).hasSize(2);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("An existing "
      + "organisation"));
    physicalMeters.save(newPhysicalMeter(organisation, DEFAULT_MEDIUM));

    messageHandler.handle(newStructureMessage(DEFAULT_MEDIUM));

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void measurementIsUpdated() {
    UUID meterId = UUID.randomUUID();
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    logicalMeters.save(new LogicalMeter(
      meterId,
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    messageHandler.handle(newMeasurementMessage("Wattage", "W", 1.0));
    messageHandler.handle(newMeasurementMessage("Wattage", "W", 2.0));

    List<Measurement> actual = measurements.findAll(new MockRequestParameters());
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).value).isEqualTo(2.0);
  }

  @Test
  public void measurementForNewQuantityIsNotUpdated() {
    UUID meterId = UUID.randomUUID();
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    logicalMeters.save(new LogicalMeter(
      meterId,
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    messageHandler.handle(newMeasurementMessage("Wattage", "W", 1.0));
    messageHandler.handle(newMeasurementMessage("Flow", "m³/s", 2.0));

    List<Measurement> actual = measurements.findAll(new MockRequestParameters());
    assertThat(actual).hasSize(2);
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));

    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      newPhysicalMeter(organisation, "Electricity")
    );

    messageHandler.handle(newMeasurementMessage());

    Measurement expectedMeasurement = new Measurement(
      1L,
      EXPECTED_DATETIME,
      DEFAULT_QUANTITY,
      1.0,
      "kWh",
      expectedPhysicalMeter
    );
    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  @Test
  public void createsLogicalMeterForMeasurement() {
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));

    messageHandler.handle(newMeasurementMessage());

    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      DEFAULT_EXTERNAL_ID
    )).isNotEmpty();
  }

  @Test
  public void createsOrganisationAndMeterForMeasurement() {
    messageHandler.handle(newMeasurementMessage());

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      DEFAULT_EXTERNAL_ID,
      DEFAULT_ADDRESS
    ).get();
    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      DEFAULT_EXTERNAL_ID
    ).get();

    Measurement expectedMeasurement = new Measurement(
      1L,
      EXPECTED_DATETIME,
      DEFAULT_QUANTITY,
      1.0,
      "kWh",
      new PhysicalMeter(
        physicalMeter.id,
        organisation,
        DEFAULT_ADDRESS,
        DEFAULT_EXTERNAL_ID,
        "Unknown meter",
        "UNKNOWN",
        0
      ).withLogicalMeterId(logicalMeter.id)
    );
    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  @Test
  public void ignoresAlarmsWithoutCrashing() {
    assertThat(messageHandler.handle(newAlarmMessageWithoutAlarms())).isEmpty();
    assertThat(messageHandler.handle(newAlarmMessageWithTwoAlarms())).isEmpty();
  }

  @Test
  public void hotWaterMeterIsMappedFromMedium() {
    assertThat(messageHandler.selectMeterDefinition(DEFAULT_MEDIUM))
      .isEqualTo(MeterDefinition.HOT_WATER_METER);
  }

  @Test
  public void districtHeatingMeterIsMappedFromMedium() {
    assertThat(messageHandler.selectMeterDefinition("District heating meter"))
      .isEqualTo(MeterDefinition.DISTRICT_HEATING_METER);
  }

  @Test
  public void unknownMediumIsMappedToUnknownMeterDefinition() {
    assertThat(messageHandler.selectMeterDefinition("Some unsupported, unknown medium"))
      .isEqualTo(MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void unknowMediumIsMappedFromEmptyValueSet() {
    List<ValueDto> districtHeatingMeterValues = emptyList();
    assertThat(messageHandler.selectMeterDefinition(districtHeatingMeterValues)).isEqualTo(
      MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void unknowMediumIsMappedFromUnknownValueSet() {
    List<ValueDto> districtHeatingMeterValues = singletonList(
      new ValueDto(LocalDateTime.now(), 0.0, "MW", "UnknownQuantity")
    );
    assertThat(messageHandler.selectMeterDefinition(districtHeatingMeterValues)).isEqualTo(
      MeterDefinition.UNKNOWN_METER);
  }

  @Test
  public void districtHeatingMeterIsMappedFromValueQuantities() {
    List<ValueDto> districtHeatingMeterValues = asList(
      newValueDto("Return temp."),
      newValueDto("Difference temp."),
      newValueDto("Flow temp."),
      newValueDto("Volume flow"),
      newValueDto("Power"),
      newValueDto("Volume"),
      newValueDto(DEFAULT_QUANTITY)
    );
    assertThat(messageHandler.selectMeterDefinition(districtHeatingMeterValues)).isEqualTo(
      MeterDefinition.DISTRICT_HEATING_METER);
  }

  @Test
  public void usesOrganisationExternalIdForMeasurementMessage() {
    Organisation existingOrganisation = new Organisation(
      UUID.randomUUID(),
      "An organisation",
      "an-organisation",
      "An external organisation ID"
    );
    organisations.save(
      existingOrganisation
    );

    MeteringMeasurementMessageDto message = newMeasurementMessage("An external organisation ID");

    messageHandler.handle(message);

    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0).physicalMeter.organisation)
      .isEqualTo(existingOrganisation);
  }

  @Test
  public void createdOrganisationIsSlugged() {
    MeteringMeasurementMessageDto message = newMeasurementMessage("Some Organisation");

    messageHandler.handle(message);

    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0).physicalMeter.organisation.slug).isEqualTo(
      "some-organisation");
  }

  @Test
  public void gatewayIsCreatedFromMeasurementMessage() {
    MeteringMeasurementMessageDto message = newMeasurementMessage();

    messageHandler.handle(message);

    assertThat(gateways.findAll(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void expectedIntervalIsSetForCreatedPhysicalMeter() {
    MeteringMeterStructureMessageDto message = newStructureMessage(60);

    messageHandler.handle(message);

    PhysicalMeter createdMeter = physicalMeters.findAll().get(0);
    assertThat(createdMeter.readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void expectedIntervalIsUpdatedForCreatedPhysicalMeter() {

    messageHandler.handle(newStructureMessage(15));
    messageHandler.handle(newStructureMessage(60));

    List<PhysicalMeter> all = physicalMeters.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void measurementUnitIsUpdated() {
    messageHandler.handle(newMeasurementMessage("kW", 1.0));

    List<Measurement> all = measurements.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("kW");
    assertThat(all.get(0).value).isEqualTo(1.0);

    messageHandler.handle(newMeasurementMessage("MW", 1.0));

    all = measurements.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("MW");
    assertThat(all.get(0).value).isEqualTo(1.0);
  }

  @Test
  public void emptyExternalIdIsRejected() {
    MeteringMeterStructureMessageDto message = newStructureMessage(
      "medium",
      "manufacturer",
      "meter-id",
      15,
      UNKNOWN_LOCATION,
      ""
    );

    messageHandler.handle(message);

    assertThat(logicalMeters.findAll(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void measurementValueIsUpdated() {
    messageHandler.handle(newMeasurementMessage(1.0));

    List<Measurement> all = measurements.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0).value).isEqualTo(1.0);

    messageHandler.handle(newMeasurementMessage(2.0));

    all = measurements.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0).value).isEqualTo(2.0);
  }

  @Test
  public void measurementValueForMissingMeterTriggersGetReferenceResponse() {
    assertThat(messageHandler.handle(newMeasurementMessage()).get()).isEqualTo(
      new GetReferenceInfoDto(
        DEFAULT_ORGANISATION_EXTERNAL_ID,
        DEFAULT_EXTERNAL_ID,
        DEFAULT_GATEWAY_EXTERNAL_ID
      )
    );
  }

  @Test
  public void measurementValueForMissingPhysicalMeterTriggersGetReferenceResponse() {
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      DEFAULT_EXTERNAL_ID,
      organisation.id,
      MeterDefinition.HOT_WATER_METER,
      UNKNOWN_LOCATION
    ));

    assertThat(messageHandler.handle(newMeasurementMessage()).get())
      .isEqualTo(new GetReferenceInfoDto(
        DEFAULT_ORGANISATION_EXTERNAL_ID,
        DEFAULT_EXTERNAL_ID,
        DEFAULT_GATEWAY_EXTERNAL_ID
      ));
  }

  @Test
  public void measurementValueForExistingGatewayDoesNotModifyGateway() {

    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    Gateway existingGateway = gateways.save(newGateway(
      organisation.id,
      DEFAULT_GATEWAY_EXTERNAL_ID
    ));

    messageHandler.handle(newMeasurementMessage());

    List<Gateway> all = gateways.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0)).isEqualTo(existingGateway);
  }

  @Test
  public void measurementValueForExistingGatewayTriggersResponseNotIncludingTheGateway() {
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    gateways.save(newGateway(organisation.id, DEFAULT_GATEWAY_EXTERNAL_ID));

    assertThat(messageHandler.handle(newMeasurementMessage()).get()).isEqualTo(
      new GetReferenceInfoDto(
        DEFAULT_ORGANISATION_EXTERNAL_ID,
        DEFAULT_EXTERNAL_ID,
        null
      )
    );
  }

  @Test
  public void measurementValueForExistingEntitiesTriggersNoResponse() {
    Organisation organisation = organisations.save(
      newOrganisation(DEFAULT_ORGANISATION_EXTERNAL_ID));
    gateways.save(newGateway(organisation.id, DEFAULT_GATEWAY_EXTERNAL_ID));
    physicalMeters.save(newPhysicalMeter(
      organisation,
      DEFAULT_MEDIUM
    ));
    logicalMeters.save(
      new LogicalMeter(
        UUID.randomUUID(),
        DEFAULT_EXTERNAL_ID,
        organisation.id,
        MeterDefinition.HOT_WATER_METER,
        UNKNOWN_LOCATION
      )
    );

    assertThat(messageHandler.handle(newMeasurementMessage())).isEmpty();
  }

  @Test
  public void meterStatusIsSetForNewMeter() {
    messageHandler.handle(newStructureMessage(StatusType.OK));

    List<MeterStatusLog> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void sameMeterStatusIsUnchangedForMeter() {
    messageHandler.handle(newStructureMessage(StatusType.OK));
    messageHandler.handle(newStructureMessage(StatusType.OK));

    List<MeterStatusLog> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void newMeterStatusChangesStatus() {
    messageHandler.handle(newStructureMessage(StatusType.OK));
    messageHandler.handle(newStructureMessage(StatusType.ERROR));

    List<MeterStatusLog> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();

  }

  private PhysicalMeter newPhysicalMeter(
    Organisation organisation,
    String defaultMedium
  ) {
    return new PhysicalMeter(
      UUID.randomUUID(),
      DEFAULT_ADDRESS,
      DEFAULT_EXTERNAL_ID,
      defaultMedium,
      DEFAULT_MANUFACTURER,
      organisation,
      DEFAULT_EXPECTED_INTERVAL
    );
  }

  private ValueDto newValueDto(String quantity) {
    return new ValueDto(LocalDateTime.now(), 0.0, "one", quantity);
  }

  private LogicalMeter findLogicalMeter() {
    Organisation organisation = findOrganisation();
    return logicalMeters.findByOrganisationIdAndExternalId(organisation.id, DEFAULT_EXTERNAL_ID)
      .get();
  }

  private Organisation findOrganisation() {
    return organisations.findBySlug(DEFAULT_ORGANISATION_EXTERNAL_ID).get();
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(double value) {
    return newMeasurementMessage(
      DEFAULT_ORGANISATION_EXTERNAL_ID,
      DEFAULT_QUANTITY,
      DEFAULT_UNIT,
      value
    );
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(String organisationExternalId) {
    return newMeasurementMessage(organisationExternalId, DEFAULT_QUANTITY, DEFAULT_UNIT, 1.0);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(
    String quantity,
    String unit,
    double value
  ) {
    return newMeasurementMessage(DEFAULT_ORGANISATION_EXTERNAL_ID, quantity, unit, value);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(String unit, double value) {
    return newMeasurementMessage(DEFAULT_ORGANISATION_EXTERNAL_ID, DEFAULT_QUANTITY, unit, value);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage() {
    return newMeasurementMessage(
      DEFAULT_ORGANISATION_EXTERNAL_ID,
      DEFAULT_QUANTITY,
      DEFAULT_UNIT,
      1.0
    );
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(
    String organisationExternalId,
    String quantity,
    String unit,
    double value
  ) {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto(DEFAULT_GATEWAY_EXTERNAL_ID),
      new MeterIdDto(DEFAULT_ADDRESS),
      new FacilityIdDto(DEFAULT_EXTERNAL_ID),
      organisationExternalId,
      "Elvaco Metering",
      singletonList(new ValueDto(MEASUREMENT_TIMESTAMP, value, unit, quantity))
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    String medium,
    String physicalMeterId
  ) {
    return newStructureMessage(
      medium,
      "KAM",
      physicalMeterId,
      DEFAULT_EXPECTED_INTERVAL,
      DEFAULT_LOCATION
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(StatusType meterStatus) {
    return newStructureMessage(
      DEFAULT_MEDIUM,
      DEFAULT_MANUFACTURER,
      DEFAULT_ADDRESS,
      DEFAULT_EXPECTED_INTERVAL,
      DEFAULT_LOCATION,
      DEFAULT_EXTERNAL_ID,
      meterStatus
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(Location location) {
    return newStructureMessage(
      DEFAULT_MEDIUM,
      DEFAULT_MANUFACTURER,
      DEFAULT_ADDRESS,
      DEFAULT_EXPECTED_INTERVAL,
      location
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    String medium
  ) {
    return newStructureMessage(
      medium,
      DEFAULT_MANUFACTURER,
      DEFAULT_ADDRESS,
      DEFAULT_EXPECTED_INTERVAL,
      DEFAULT_LOCATION
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    int expectedInterval
  ) {
    return newStructureMessage(
      DEFAULT_MEDIUM,
      DEFAULT_MANUFACTURER,
      DEFAULT_ADDRESS,
      expectedInterval,
      DEFAULT_LOCATION
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    int expectedInterval,
    Location location
  ) {
    return newStructureMessage(
      medium,
      manufacturer,
      physicalMeterId,
      expectedInterval,
      location,
      DEFAULT_EXTERNAL_ID
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    int expectedInterval,
    Location location,
    String externalId
  ) {
    return newStructureMessage(
      medium,
      manufacturer,
      physicalMeterId,
      expectedInterval,
      location,
      externalId,
      StatusType.OK
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    int expectedInterval,
    Location location,
    String externalId,
    StatusType meterStatus
  ) {
    return new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      new MeterDto(physicalMeterId, medium, meterStatus.name(), manufacturer, expectedInterval),
      new FacilityDto(
        externalId,
        location.getCountry(),
        location.getCity(),
        location.getAddress()
      ),
      "Test source system",
      DEFAULT_ORGANISATION_EXTERNAL_ID,
      new GatewayStatusDto(
        DEFAULT_GATEWAY_EXTERNAL_ID,
        DEFAULT_PRODUCT_MODEL,
        meterStatus.name()
      )
    );
  }

  private MeteringAlarmMessageDto newAlarmMessageWithoutAlarms() {
    return new MeteringAlarmMessageDto(
      MessageType.METERING_ALARM_V_1_0,
      new GatewayIdDto("351"),
      new MeterIdDto("sdf"),
      new FacilityIdDto("asdfg2"),
      "ICA Maxi",
      "Elvaco Metering",
      emptyList()
    );
  }

  private MeteringAlarmMessageDto newAlarmMessageWithTwoAlarms() {
    List<AlarmDto> alarms = new ArrayList<>();
    alarms.add(new AlarmDto(LocalDateTime.parse("2018-04-09T13:45:02"), 88));
    alarms.add(new AlarmDto(LocalDateTime.parse("2009-05-16T14:14:06"), 99));
    return new MeteringAlarmMessageDto(
      MessageType.METERING_ALARM_V_1_0,
      new GatewayIdDto("351"),
      new MeterIdDto("sdf"),
      new FacilityIdDto("asdfg2"),
      "ICA Maxi",
      "Elvaco Metering",
      alarms
    );
  }

  private Gateway newGateway(UUID organisationId, String serial) {
    return new Gateway(UUID.randomUUID(), organisationId, serial, DEFAULT_PRODUCT_MODEL);
  }

  private Organisation newOrganisation(String code) {
    return newOrganisation("", code);
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(randomUUID(), name, code);
  }
}
