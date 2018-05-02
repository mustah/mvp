package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.geocode.MockGeocodeService;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMetersWithCascading;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MeteringStructureMessageConsumerTest {

  private static final String MANUFACTURER = "ELV";
  private static final String PRODUCT_MODEL = "CMi2110";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final int EXPECTED_INTERVAL = 15;
  private static final String HOT_WATER_MEDIUM = "Hot water";
  private static final String ADDRESS = "1234";
  private static final String ORGANISATION_SLUG = "some-organisation";
  private static final String EXTERNAL_ID = "ABC-123";
  private static final Location LOCATION_KUNGSBACKA = new LocationBuilder()
    .country("Sweden")
    .city("Kungsbacka")
    .address("Kabelgatan 2T")
    .build();

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Gateways gateways;
  private StructureMessageConsumer messageHandler;
  private MockGeocodeService geocodeService;

  @Before
  public void setUp() {
    User superAdmin = new UserBuilder()
      .name("super-admin")
      .email("super@admin.io")
      .password("password")
      .language(Language.en)
      .organisationElvaco()
      .asSuperAdmin()
      .build();
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .language(Language.en)
        .organisation(new Organisation(randomUUID(), "some organisation", ORGANISATION_SLUG))
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    physicalMeters = new MockPhysicalMeters();
    organisations = new MockOrganisations();
    logicalMeters = new MockLogicalMetersWithCascading(physicalMeters);
    gateways = new MockGateways();
    geocodeService = new MockGeocodeService();

    messageHandler = new MeteringStructureMessageConsumer(
      new LogicalMeterUseCases(
        authenticatedUser,
        logicalMeters,
        new MockMeasurements()
      ),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters),
      new OrganisationUseCases(
        authenticatedUser,
        organisations,
        new OrganisationPermissions(new MockUsers(singletonList(superAdmin)))
      ),
      new GatewayUseCases(gateways, authenticatedUser),
      geocodeService
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get();

    PhysicalMeter savedPhysicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      EXTERNAL_ID,
      ADDRESS
    ).get();

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      logicalMeter.id,
      EXTERNAL_ID,
      organisation.id,
      new LocationBuilder().country("Sweden").city("Kungsbacka").address("Kabelgatan 2T").build(),
      logicalMeter.created,
      singletonList(savedPhysicalMeter),
      MeterDefinition.HOT_WATER_METER,
      singletonList(gateways.findBy(
        organisation.id,
        PRODUCT_MODEL,
        GATEWAY_EXTERNAL_ID
      ).get())
    );

    assertThat(logicalMeter).isEqualTo(expectedLogicalMeter);
    assertThat(savedPhysicalMeter).isEqualTo(new PhysicalMeter(
      savedPhysicalMeter.id,
      organisation,
      ADDRESS,
      EXTERNAL_ID,
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      logicalMeter.id,
      EXPECTED_INTERVAL,
      null,
      savedPhysicalMeter.statuses
    ));
  }

  @Test
  public void updatesExistingGatewayWithUnknownManufacturer() {
    UUID gatewayId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(new Gateway(
      gatewayId,
      organisation.id,
      GATEWAY_EXTERNAL_ID,
      "Unknown"
    ));

    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    Gateway gateway = gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get();
    assertThat(gateway.id).isEqualTo(gatewayId);
    assertThat(gateway.productModel).isEqualTo(PRODUCT_MODEL);
  }

  @Test
  public void locationIsUpdatedForExistingMeter() {
    UUID meterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(new LogicalMeter(
      meterId,
      EXTERNAL_ID,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now()

    ));

    Location newLocation = new LocationBuilder()
      .country("")
      .city("Växjö")
      .address("Gatvägen 41")
      .build();
    messageHandler.accept(newStructureMessage(newLocation));

    assertThat(logicalMeters.findById(meterId).get().location).isEqualTo(newLocation);
  }

  @Test
  public void createsOrganisationWithSameNameAsCode() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    assertThat(organisation.name).isEqualTo(ORGANISATION_SLUG);
  }

  @Test
  public void createsMeterAndGatewayForExistingOrganisation() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(logicalMeters.findAll(new MockRequestParameters())).hasSize(1);
    assertThat(organisations.findAll()).hasSize(1);
    PhysicalMeter meter = allPhysicalMeters.get(0);
    assertThat(meter.organisation).isEqualTo(organisation);

    LogicalMeter logicalMeter = logicalMeters.findById(meter.logicalMeterId).get();
    assertThat(logicalMeter.meterDefinition).isEqualTo(MeterDefinition.HOT_WATER_METER);
    assertThat(gateways.findBy(organisation.id, PRODUCT_MODEL, GATEWAY_EXTERNAL_ID)
      .isPresent()).isTrue();
  }

  @Test
  public void addsPhysicalMeterToExistingLogicalMeter() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    LogicalMeter saved = findLogicalMeter();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(saved.id);
  }

  @Test
  public void resendingSameMessageShouldNotUpdateExistingGateways() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    List<Gateway> allAfterFirstMessage = gateways.findAll(new MockRequestParameters());
    assertThat(allAfterFirstMessage).hasSize(1);

    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    assertThat(gateways.findAll(new MockRequestParameters())).isEqualTo(allAfterFirstMessage);
  }

  @Test
  public void gatewaysAreConnectedToMeters() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

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
    messageHandler.accept(newStructureMessage("Unmappable medium"));

    List<LogicalMeter> meters = logicalMeters.findAll(new MockRequestParameters());
    assertThat(meters).hasSize(1);
    assertThat(meters.get(0).getMedium()).isEqualTo("Unknown medium");
  }

  @Test
  public void callsGeocodeService() {
    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    LocationWithId expectedLocationWithId = new LocationBuilder()
      .country("Sweden")
      .city("Kungsbacka")
      .address("Kabelgatan 2T")
      .id(geocodeService.requestId)
      .buildLocationWithId();

    assertThat(geocodeService.requestId).isNotNull();
    assertThat(geocodeService.location).isEqualTo(expectedLocationWithId);
  }

  @Test
  public void addsSecondPhysicalMeterToExistingLogicalMeter() {
    Organisation organisation = organisations.save(
      newOrganisation("An existing organisation", ORGANISATION_SLUG)
    );
    ZonedDateTime now = ZonedDateTime.now();
    UUID logicalMeterId = randomUUID();
    PhysicalMeter existingPhysicalMeter = physicalMeters.save(newPhysicalMeter(
      organisation,
      HOT_WATER_MEDIUM
    ).withLogicalMeterId(logicalMeterId));

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      EXTERNAL_ID,
      organisation.id,
      UNKNOWN_LOCATION,
      now,
      singletonList(existingPhysicalMeter),
      MeterDefinition.HOT_WATER_METER,
      emptyList()
    ));

    MeteringStructureMessageDto structureMessage =
      newStructureMessage(HOT_WATER_MEDIUM, "4321");

    messageHandler.accept(structureMessage);

    List<LogicalMeter> organisationMeters = logicalMeters.findByOrganisationId(organisation.id);
    assertThat(organisationMeters).hasSize(1);
    LogicalMeter meter = organisationMeters.get(0);
    assertThat(meter.physicalMeters).hasSize(2);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("An existing organisation"));
    physicalMeters.save(newPhysicalMeter(organisation, HOT_WATER_MEDIUM));

    messageHandler.accept(newStructureMessage(HOT_WATER_MEDIUM));

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void expectedIntervalIsSetForCreatedPhysicalMeter() {
    MeteringStructureMessageDto message = newStructureMessage(60);

    messageHandler.accept(message);

    PhysicalMeter createdMeter = physicalMeters.findAll().get(0);
    assertThat(createdMeter.readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void expectedIntervalIsUpdatedForCreatedPhysicalMeter() {
    messageHandler.accept(newStructureMessage(15));
    messageHandler.accept(newStructureMessage(60));

    List<PhysicalMeter> all = physicalMeters.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void emptyExternalIdIsRejected() {
    MeteringStructureMessageDto message = newStructureMessage(
      "medium",
      "manufacturer",
      "meter-id",
      15,
      UNKNOWN_LOCATION,
      ""
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAll(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void meterStatusIsSetForNewMeter() {
    messageHandler.accept(newStructureMessageWithMeterStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void sameMeterStatusIsUnchangedForMeter() {
    messageHandler.accept(newStructureMessageWithMeterStatus(StatusType.OK));
    messageHandler.accept(newStructureMessageWithMeterStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void newMeterStatusChangesStatus() {
    messageHandler.accept(newStructureMessageWithMeterStatus(StatusType.OK));
    messageHandler.accept(newStructureMessageWithMeterStatus(StatusType.ERROR));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  @Test
  public void newStatusIsSetForGateway() {
    messageHandler.accept(newStructureMessageWithGatewayStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;

    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void sameGatewayStatusIsUnchangedForGateway() {
    messageHandler.accept(newStructureMessageWithGatewayStatus(StatusType.OK));
    messageHandler.accept(newStructureMessageWithGatewayStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void newGatewayStatusChangesStatus() {
    messageHandler.accept(newStructureMessageWithGatewayStatus(StatusType.OK));
    messageHandler.accept(newStructureMessageWithGatewayStatus(StatusType.ERROR));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;

    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(newOrganisation(ORGANISATION_SLUG));
  }

  private PhysicalMeter newPhysicalMeter(
    Organisation organisation,
    String defaultMedium
  ) {
    return new PhysicalMeter(
      randomUUID(),
      ADDRESS,
      EXTERNAL_ID,
      defaultMedium,
      MANUFACTURER,
      organisation,
      EXPECTED_INTERVAL
    );
  }

  private LogicalMeter findLogicalMeter() {
    Organisation organisation = findOrganisation();
    return logicalMeters.findByOrganisationIdAndExternalId(organisation.id, EXTERNAL_ID)
      .get();
  }

  private Organisation findOrganisation() {
    return organisations.findBySlug(ORGANISATION_SLUG).get();
  }

  private MeteringStructureMessageDto newStructureMessageWithGatewayStatus(
    StatusType gatewayStatus
  ) {
    return newStructureMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      EXPECTED_INTERVAL,
      LOCATION_KUNGSBACKA,
      EXTERNAL_ID,
      StatusType.OK,
      gatewayStatus
    );
  }

  private MeteringStructureMessageDto newStructureMessageWithMeterStatus(
    StatusType meterStatus
  ) {
    return newStructureMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      EXPECTED_INTERVAL,
      LOCATION_KUNGSBACKA,
      EXTERNAL_ID,
      meterStatus,
      StatusType.OK
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
    String medium,
    String physicalMeterId
  ) {
    return newStructureMessage(
      medium,
      "KAM",
      physicalMeterId,
      EXPECTED_INTERVAL,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringStructureMessageDto newStructureMessage(Location location) {
    return newStructureMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      EXPECTED_INTERVAL,
      location
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
    String medium
  ) {
    return newStructureMessage(
      medium,
      MANUFACTURER,
      ADDRESS,
      EXPECTED_INTERVAL,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
    int expectedInterval
  ) {
    return newStructureMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      expectedInterval,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
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
      EXTERNAL_ID
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
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
      StatusType.OK,
      StatusType.OK
    );
  }

  private MeteringStructureMessageDto newStructureMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    int expectedInterval,
    Location location,
    String externalId,
    StatusType meterStatus,
    StatusType gatewayStatus
  ) {
    return new MeteringStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      new MeterDto(physicalMeterId, medium, meterStatus.name(), manufacturer, expectedInterval),
      new FacilityDto(
        externalId,
        location.getCountry(),
        location.getCity(),
        location.getAddress()
      ),
      "Test source system",
      ORGANISATION_SLUG,
      new GatewayStatusDto(
        GATEWAY_EXTERNAL_ID,
        PRODUCT_MODEL,
        gatewayStatus.name()
      )
    );
  }

  private Organisation newOrganisation(String code) {
    return newOrganisation("", code);
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(randomUUID(), name, code);
  }
}
