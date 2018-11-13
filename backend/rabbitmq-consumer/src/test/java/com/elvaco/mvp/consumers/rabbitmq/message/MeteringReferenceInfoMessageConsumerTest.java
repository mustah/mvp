package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
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
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testing.amqp.MockJobService;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.geocode.MockGeocodeService;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMetersWithCascading;
import com.elvaco.mvp.testing.repository.MockMeterStatusLogs;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockProperties;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.testing.fixture.LocationTestData.locationWithoutCoordinates;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MeteringReferenceInfoMessageConsumerTest {

  private static final String MANUFACTURER = "ELV";
  private static final String PRODUCT_MODEL = "CMi2110";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final String FIFTEEN_MINUTE_CRON = "*/15 * * * *";
  private static final String HOUR_CRON = "0 * * * *";
  private static final Integer REVISION_ONE = 1;
  private static final Integer MBUS_METER_TYPE_ONE = 1;
  private static final Integer READ_INTERVAL_IN_MINUTES = 15;
  private static final String HOT_WATER_MEDIUM = "Hot water";
  private static final String ADDRESS = "1234";
  private static final String ORGANISATION_EXTERNAL_ID = "Some Organisation";
  private static final String ORGANISATION_SLUG = "some-organisation";
  private static final String EXTERNAL_ID = "ABC-123";
  private static final Location LOCATION_KUNGSBACKA = locationWithoutCoordinates().build();
  private static final long EXPECTED_DEFAULT_READ_INTERVAL = 60L;

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Gateways gateways;
  private ReferenceInfoMessageConsumer messageHandler;
  private MockGeocodeService geocodeService;
  private PropertiesUseCases propertiesUseCases;
  private MockMeterStatusLogs meterStatusLogs;
  private MockJobService<MeteringReferenceInfoMessageDto> jobService;

  @Before
  public void setUp() {
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .organisation(new Organisation(
          randomUUID(),
          ORGANISATION_EXTERNAL_ID,
          ORGANISATION_SLUG,
          ORGANISATION_EXTERNAL_ID
        ))
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    physicalMeters = new MockPhysicalMeters();
    organisations = new MockOrganisations();
    logicalMeters = new MockLogicalMetersWithCascading(physicalMeters);
    gateways = new MockGateways();
    geocodeService = new MockGeocodeService();
    propertiesUseCases = new PropertiesUseCases(authenticatedUser, new MockProperties());

    meterStatusLogs = new MockMeterStatusLogs();
    jobService = new MockJobService<>();
    messageHandler = new MeteringReferenceInfoMessageConsumer(
      new LogicalMeterUseCases(authenticatedUser, logicalMeters),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters, meterStatusLogs),
      new OrganisationUseCases(
        authenticatedUser,
        organisations,
        new OrganisationPermissions(new MockUsers(singletonList(
          new UserBuilder()
            .name("super-admin")
            .email("super@admin.io")
            .password("password")
            .organisationElvaco()
            .asSuperAdmin()
            .build()
        )))
      ),
      new GatewayUseCases(gateways, authenticatedUser),
      geocodeService,
      propertiesUseCases,
      jobService
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    messageHandler.accept(messageBuilder().productModel(PRODUCT_MODEL)
      .gatewayExternalId(GATEWAY_EXTERNAL_ID)
      .medium(HOT_WATER_MEDIUM)
      .build());

    Organisation organisation = findOrganisation();

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get();

    PhysicalMeter savedPhysicalMeter = findPhysicalMeterByOrganisationId(organisation);

    LogicalMeter expectedLogicalMeter = LogicalMeter.builder()
      .id(logicalMeter.id)
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .created(logicalMeter.created)
      .location(locationWithoutCoordinates().build())
      .build();

    Gateway gateway = gateways.findBy(organisation.id, PRODUCT_MODEL, GATEWAY_EXTERNAL_ID).get();

    assertThat(logicalMeter).isEqualTo(expectedLogicalMeter);
    assertThat(savedPhysicalMeter).isEqualTo(PhysicalMeter.builder()
      .id(savedPhysicalMeter.id)
      .organisation(organisation)
      .address(ADDRESS)
      .externalId(EXTERNAL_ID)
      .medium(HOT_WATER_MEDIUM)
      .manufacturer(MANUFACTURER)
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(READ_INTERVAL_IN_MINUTES)
      .revision(REVISION_ONE)
      .mbusDeviceType(MBUS_METER_TYPE_ONE)
      .statuses(savedPhysicalMeter.statuses)
      .build());
    assertThat(gateway.meters).extracting("id").containsExactly(logicalMeter.id);
  }

  @Test
  public void updatesExistingGatewayWithNewProductModel() {
    UUID gatewayId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(Gateway.builder()
      .id(gatewayId)
      .organisationId(organisation.id)
      .serial(GATEWAY_EXTERNAL_ID)
      .productModel("OldValue")
      .build());

    messageHandler.accept(messageBuilder().gatewayExternalId(GATEWAY_EXTERNAL_ID)
      .productModel(PRODUCT_MODEL)
      .build());

    Gateway gateway = gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get();
    assertThat(gateway.id).isEqualTo(gatewayId);
    assertThat(gateway.productModel).isEqualTo(PRODUCT_MODEL);
  }

  @Test
  public void locationIsUpdatedForExistingMeter() {
    UUID meterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .build());

    Location newLocation = new LocationBuilder()
      .country("")
      .city("Växjö")
      .address("Gatvägen 41")
      .build();
    messageHandler.accept(messageBuilder().location(newLocation).build());

    assertThat(logicalMeters.findById(meterId).get().location).isEqualTo(newLocation);
  }

  @Test
  public void createsOrganisationWithSameNameAsExternalId() {
    messageHandler.accept(messageBuilder()
      .organisationExternalId(ORGANISATION_EXTERNAL_ID)
      .build());

    Organisation organisation = findOrganisation();

    assertThat(organisation.name).isEqualTo(ORGANISATION_EXTERNAL_ID);
  }

  @Test
  public void createsMeterAndGatewayForExistingOrganisation() {
    messageHandler.accept(messageBuilder()
      .medium(HOT_WATER_MEDIUM)
      .productModel(PRODUCT_MODEL)
      .gatewayExternalId(GATEWAY_EXTERNAL_ID)
      .build());

    Organisation organisation = findOrganisation();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);
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
    messageHandler.accept(messageBuilder().build());

    LogicalMeter saved = findLogicalMeter();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(saved.id);
  }

  @Test
  public void resendingSameMessageShouldNotUpdateExistingGateways() {
    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    List<Gateway> allAfterFirstMessage = gateways.findAll();
    assertThat(allAfterFirstMessage).hasSize(1);

    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    assertThat(gateways.findAll()).isEqualTo(allAfterFirstMessage);
  }

  @Test
  public void gatewaysAreConnectedToMeters() {
    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    List<Gateway> all = gateways.findAll();
    assertThat(all.stream().anyMatch(gateway -> gateway.meters.isEmpty())).isFalse();
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    messageHandler.accept(messageBuilder().medium("Unmappable medium").build());

    List<LogicalMeter> meters = logicalMeters.findAllWithDetails(new MockRequestParameters());
    assertThat(meters).hasSize(1);
    assertThat(meters.get(0).getMedium()).isEqualTo("Unknown medium");
  }

  @Test
  public void updatesMeterDefinitionForExistingLogicalMeter() {
    messageHandler.accept(messageBuilder().medium("Unknown medium").build());

    LogicalMeter meter = logicalMeters.findAllWithDetails(new MockRequestParameters()).get(0);
    assertThat(meter.getMedium()).isEqualTo("Unknown medium");

    messageHandler.accept(messageBuilder().medium("Heat, Return temp").build());

    meter = logicalMeters.findAllWithDetails(new MockRequestParameters()).get(0);
    assertThat(meter.meterDefinition.type).isEqualTo(MeterDefinition.DISTRICT_HEATING_METER.type);
  }

  @Test
  public void doesNotUpdateMeterDefinitionWithUnmappableMedium() {
    messageHandler.accept(messageBuilder().medium("Unknown medium").build());

    LogicalMeter meter = logicalMeters.findAllWithDetails(new MockRequestParameters()).get(0);
    assertThat(meter.getMedium()).isEqualTo("Unknown medium");

    messageHandler.accept(messageBuilder().medium("I don't even know what this is?").build());

    meter = logicalMeters.findAllWithDetails(new MockRequestParameters()).get(0);
    assertThat(meter.meterDefinition.type).isEqualTo(MeterDefinition.UNKNOWN_METER.type);
  }

  @Test
  public void updatesManufacturerForExistingMeter() {
    messageHandler.accept(messageBuilder().manufacturer("ELV").build());

    LogicalMeter meter = logicalMeters.findAllWithDetails(new MockRequestParameters()).get(0);

    List<PhysicalMeter> all = physicalMeters.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).manufacturer).isEqualTo("ELV");
    assertThat(physicalMeters.findAll().stream().map(pm -> pm.logicalMeterId).collect(toList()))
      .containsExactly(meter.id);

    // Add same message with different manufacturer
    messageHandler.accept(messageBuilder().manufacturer("KAM").build());

    assertThat(physicalMeters.findAll().get(0).manufacturer).isEqualTo("KAM");
    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void callsGeocodeService() {
    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    LocationWithId expectedLocationWithId = locationWithoutCoordinates()
      .id(geocodeService.requestId)
      .buildLocationWithId();

    assertThat(geocodeService.requestId).isNotNull();
    assertThat(geocodeService.location).isEqualTo(expectedLocationWithId);
  }

  @Test
  public void forceUpdateGeolocation_WhenFlagIsEnabledAndClearFlag() {
    UUID organisationId = saveDefaultOrganisation().getId();

    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    LocationBuilder builder = locationWithoutCoordinates()
      .id(geocodeService.requestId);

    assertThat(geocodeService.location).isEqualTo(builder.buildLocationWithId());

    propertiesUseCases.forceUpdateGeolocation(geocodeService.requestId, organisationId);

    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    Property.Id id = Property.idOf(
      geocodeService.requestId,
      organisationId,
      FeatureType.UPDATE_GEOLOCATION.key
    );

    assertThat(geocodeService.location).isEqualTo(builder.forceUpdate().buildLocationWithId());
    assertThat(propertiesUseCases.findById(id).isPresent()).isFalse();
  }

  @Test
  public void addsSecondPhysicalMeterToExistingLogicalMeter() {
    Organisation organisation = organisations.save(
      newOrganisation(ORGANISATION_EXTERNAL_ID, ORGANISATION_SLUG)
    );
    UUID logicalMeterId = randomUUID();
    physicalMeters.save(
      physicalMeter()
        .organisation(organisation)
        .logicalMeterId(logicalMeterId)
        .build()
    );

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .created(ZonedDateTime.now())
      .location(UNKNOWN_LOCATION)
      .build());

    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM)
      .physicalMeterId("4321")
      .build());

    assertThat(logicalMeters.findAllByOrganisationId(organisation.id)).hasSize(1);
    assertThat(physicalMeters.findAll().stream().map(pm -> pm.logicalMeterId))
      .isEqualTo(asList(logicalMeterId, logicalMeterId));
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("", "Organisation code"));
    physicalMeters.save(physicalMeter().organisation(organisation).build());

    messageHandler.accept(messageBuilder().medium(HOT_WATER_MEDIUM).build());

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void emptyExternalIdIsRejected() {
    MeteringReferenceInfoMessageDto message = messageBuilder().medium("medium")
      .manufacturer("manufacturer")
      .physicalMeterId("meter-id")
      .cron(FIFTEEN_MINUTE_CRON)
      .revision(REVISION_ONE)
      .mbusDeviceType(MBUS_METER_TYPE_ONE)
      .location(UNKNOWN_LOCATION)
      .externalId("")
      .build();

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void nullFacilityIdIsRejected() {
    MeteringReferenceInfoMessageDto message = new MeteringReferenceInfoMessageDto(
      null,
      new FacilityDto(null, null, null, null),
      "Test source system",
      "organisation id",
      null,
      ""
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void physicalMeterRequiresMeterId() {
    MeteringReferenceInfoMessageDto message = new MeteringReferenceInfoMessageDto(
      new MeterDto(null, null, null, null, null, null, null),
      new FacilityDto("valid facility id", null, null, null),
      "Test source system",
      "organisation id",
      null,
      ""
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).hasSize(1);
    assertThat(physicalMeters.findAll()).isEmpty();
  }

  @Test
  public void meterStatusIsSetForNewMeter() {
    messageHandler.accept(messageBuilder().meterStatus(StatusType.OK.name()).build());

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
    assertThat(meterStatusLogs.allMocks()).extracting("status").containsExactly(StatusType.OK);
  }

  @Test
  public void sameMeterStatusIsUnchangedForMeter() {
    messageHandler.accept(messageBuilder().meterStatus(StatusType.OK.name()).build());
    messageHandler.accept(messageBuilder().meterStatus(StatusType.OK.name()).build());

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
    assertThat(meterStatusLogs.allMocks()).extracting("status").containsOnly(StatusType.OK);
  }

  @Test
  public void newMeterStatusChangesStatus() {
    messageHandler.accept(messageBuilder().meterStatus(StatusType.OK.name()).build());
    messageHandler.accept(messageBuilder().meterStatus(StatusType.ERROR.name()).build());

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  @Test
  public void newStatusIsSetForGateway() {
    messageHandler.accept(messageBuilder().gatewayStatus(StatusType.OK.name()).build());

    List<StatusLogEntry<UUID>> statuses = gateways.findAll()
      .get(0).statusLogs;

    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void errorReportedStatusIsSetForMeter() {
    messageHandler.accept(messageBuilder().meterStatus("ErrorReported").build());

    assertThat(physicalMeters.findAll())
      .flatExtracting("statuses")
      .extracting("status")
      .containsExactly(StatusType.ERROR);
  }

  @Test
  public void errorReportedStatusIsSetForGateway() {
    messageHandler.accept(messageBuilder().gatewayStatus("ErrorReported").build());

    assertThat(gateways.findAll())
      .flatExtracting("statusLogs")
      .extracting("status")
      .containsExactly(StatusType.ERROR);
  }

  @Test
  public void newFacilityWithoutGatewayOrMeterIsNotSaved() {
    String externalId = "an external id";
    Location location = Location.UNKNOWN_LOCATION;
    MeteringReferenceInfoMessageDto message = new MeteringReferenceInfoMessageDto(
      null,
      new FacilityDto(
        externalId,
        location.getCountry(),
        location.getCity(),
        location.getAddress()
      ),
      "Test source system",
      "an organisation",
      null,
      ""
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithDetails(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void sameGatewayStatusIsUnchangedForGateway() {
    messageHandler.accept(messageBuilder().gatewayStatus(StatusType.OK.name()).build());
    messageHandler.accept(messageBuilder().gatewayStatus(StatusType.OK.name()).build());

    List<StatusLogEntry<UUID>> statuses = gateways.findAll()
      .get(0).statusLogs;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void newGatewayStatusChangesStatus() {
    messageHandler.accept(messageBuilder().gatewayStatus(StatusType.OK.name()).build());
    messageHandler.accept(messageBuilder().gatewayStatus(StatusType.ERROR.name()).build());

    List<StatusLogEntry<UUID>> statuses = gateways.findAll()
      .get(0).statusLogs;

    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  @Test
  public void readInterval_CreateMeterWithoutCron_UseFallback() {
    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(EXPECTED_DEFAULT_READ_INTERVAL);
  }

  @Test
  public void readInterval_ShouldNotBeReset_WhenSecondMessageHasNoCron() {
    messageHandler.accept(messageBuilder().cron(FIFTEEN_MINUTE_CRON).build());
    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(15L);
  }

  @Test
  public void readInterval_ShouldNotBeReset_WhenSecondMessageHasEmptyCron() {
    messageHandler.accept(messageBuilder().cron(FIFTEEN_MINUTE_CRON).build());
    messageHandler.accept(messageBuilder().cron("").build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(15L);
  }

  @Test
  public void readInterval_IsSetForCreatedPhysicalMeter() {
    messageHandler.accept(messageBuilder().cron(HOUR_CRON).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(60L);
  }

  @Test
  public void readInterval_IsNotUpdatedForPhysicalMeterWithZeroAsReadInterval() {
    UUID logicalMeterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    physicalMeters.save(
      physicalMeter()
        .readIntervalMinutes(0)
        .logicalMeterId(logicalMeterId)
        .organisation(organisation)
        .build()
    );

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .created(ZonedDateTime.now())
      .location(UNKNOWN_LOCATION)
      .build());

    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(0L);
  }

  @Test
  public void readInterval_IsUpdatedForCreatedPhysicalMeter() {
    messageHandler.accept(messageBuilder().cron(FIFTEEN_MINUTE_CRON).build());
    messageHandler.accept(messageBuilder().cron(HOUR_CRON).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(60L);
  }

  @Test
  public void readInterval_ShouldBeUpdated_WhenSecondMessageHasCron() {
    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(EXPECTED_DEFAULT_READ_INTERVAL);

    messageHandler.accept(messageBuilder().cron(FIFTEEN_MINUTE_CRON).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(15L);
  }

  @Test
  public void readInterval_ShouldUseFallback_WhenConsecutiveReadIntervalsAreMissing() {
    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(EXPECTED_DEFAULT_READ_INTERVAL);

    messageHandler.accept(messageBuilder().cron(null).build());

    assertThat(physicalMeters.findAll())
      .extracting("readIntervalMinutes")
      .containsExactly(EXPECTED_DEFAULT_READ_INTERVAL);
  }

  @Test
  public void emptyGatewayField_MeterIsUpdatedGatewayIsNotCreated() {
    UUID meterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(EXTERNAL_ID)
      .organisationId(organisation.id)
      .build());

    messageHandler.accept(
      messageBuilder().location(new LocationBuilder().city("Borås").build()).build()
        .withGatewayStatus(new GatewayStatusDto(null, null, null))
    );

    assertThat(logicalMeters.findById(meterId).get().location.getCity()).isEqualTo("borås");
    assertThat(gateways.findAll()).isEmpty();
  }

  @Test
  public void emptyGatewayField_MeterIsCreatedGatewayIsNotCreated() {
    Organisation organisation = saveDefaultOrganisation();

    messageHandler.accept(
      messageBuilder().location(new LocationBuilder().city("Borås").build()).build()
        .withGatewayStatus(new GatewayStatusDto(null, null, null))
    );

    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get().location.getCity()).isEqualTo("borås");
    assertThat(gateways.findAll()).isEmpty();
  }

  @Test
  public void emptyMeterField_GatewayIsUpdatedAndMeterIsCreated() {
    Organisation organisation = saveDefaultOrganisation();
    UUID gatewayId = randomUUID();

    gateways.save(Gateway.builder()
      .id(gatewayId)
      .organisationId(organisation.id)
      .serial(GATEWAY_EXTERNAL_ID)
      .productModel(PRODUCT_MODEL)
      .build());

    messageHandler.accept(
      messageBuilder().gatewayStatus(StatusType.OK.name()).build()
        .withMeter(new MeterDto(null, null, null, null, null, null, null))
    );

    assertThat(gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get()
      .currentStatus().status).isEqualTo(StatusType.OK);
    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    )).isPresent();
  }

  @Test
  public void emptyMeterField_GatewayAndMeterIsCreated() {
    Organisation organisation = saveDefaultOrganisation();

    messageHandler.accept(
      messageBuilder().gatewayStatus(StatusType.ERROR.name()).build()
        .withMeter(new MeterDto(null, null, null, null, null, null, null))
    );

    assertThat(gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get()
      .currentStatus().status).isEqualTo(StatusType.ERROR);
    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    )).isPresent();
  }

  @Test
  public void jobIdCache_NotUpdatedWhenJobIdIsEmpty() {
    messageHandler.accept(
      messageBuilder().jobId("").build()
    );

    assertThat(jobService.getAll()).isEmpty();
  }

  @Test
  public void jobIdCache_NotUpdatedWhenJobIdIsNull() {
    messageHandler.accept(
      messageBuilder().jobId(null).build()
    );

    assertThat(jobService.getAll()).isEmpty();
  }

  @Test
  public void jobIdCache_NotUpdatedWhenJobIdNotAlreadyPresentInCache() {
    messageHandler.accept(
      messageBuilder().jobId("job-id").build()
    );

    assertThat(jobService.getAll()).isEmpty();
  }

  @Test
  public void jobIdCache_isUpdatedWhenJobIdPresentInCache() {
    jobService.newPendingJob("job-id");
    MeteringReferenceInfoMessageDto messageDto = messageBuilder().jobId("job-id").build();
    messageHandler.accept(messageDto);

    assertThat(jobService.getJob("job-id")).isEqualTo(messageDto);
  }

  private PhysicalMeter findPhysicalMeterByOrganisationId(Organisation organisation) {
    return physicalMeters.findByWithStatuses(
      organisation.id,
      EXTERNAL_ID,
      ADDRESS
    ).get();
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(newOrganisation(ORGANISATION_EXTERNAL_ID, ORGANISATION_SLUG));
  }

  private LogicalMeter findLogicalMeter() {
    Organisation organisation = findOrganisation();
    return logicalMeters.findByOrganisationIdAndExternalId(organisation.id, EXTERNAL_ID)
      .get();
  }

  private Organisation findOrganisation() {
    return organisations.findBySlug(ORGANISATION_SLUG).get();
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(randomUUID(), name, code, name);
  }

  private MeteringReferenceInfoMessageDtoBuilder messageBuilder() {
    return new MeteringReferenceInfoMessageDtoBuilder();
  }

  private static PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .address(ADDRESS)
      .externalId(EXTERNAL_ID)
      .medium(HOT_WATER_MEDIUM)
      .manufacturer(MANUFACTURER)
      .readIntervalMinutes(READ_INTERVAL_IN_MINUTES);
  }

  private static class MeteringReferenceInfoMessageDtoBuilder {

    private String physicalMeterId = ADDRESS;
    private String medium = HOT_WATER_MEDIUM;
    private Integer mbusDeviceType = MBUS_METER_TYPE_ONE;
    private String manufacturer = MANUFACTURER;
    private String cron = FIFTEEN_MINUTE_CRON;
    private Integer revision = REVISION_ONE;
    private String externalId = EXTERNAL_ID;
    private Location location = LOCATION_KUNGSBACKA;
    private String gatewayStatus = StatusType.OK.name();
    private String meterStatus = StatusType.OK.name();
    private String gatewayExternalId = GATEWAY_EXTERNAL_ID;
    private String productModel = PRODUCT_MODEL;
    private String organisationExternalId = ORGANISATION_EXTERNAL_ID;
    private String jobId = "";

    public MeteringReferenceInfoMessageDtoBuilder medium(String medium) {
      this.medium = medium;
      return this;
    }

    public MeteringReferenceInfoMessageDtoBuilder manufacturer(String manufacturer) {
      this.manufacturer = manufacturer;
      return this;
    }

    public MeteringReferenceInfoMessageDtoBuilder cron(String cron) {
      this.cron = cron;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder physicalMeterId(String physicalMeterId) {
      this.physicalMeterId = physicalMeterId;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder mbusDeviceType(Integer mbusDeviceType) {
      this.mbusDeviceType = mbusDeviceType;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder revision(Integer revision) {
      this.revision = revision;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder location(Location location) {
      this.location = location;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder gatewayStatus(String gatewayStatus) {
      this.gatewayStatus = gatewayStatus;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder meterStatus(String meterStatus) {
      this.meterStatus = meterStatus;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder gatewayExternalId(String gatewayExternalId) {
      this.gatewayExternalId = gatewayExternalId;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder productModel(String productModel) {
      this.productModel = productModel;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder organisationExternalId(
      String organisationExternalId
    ) {
      this.organisationExternalId = organisationExternalId;
      return this;
    }

    private MeteringReferenceInfoMessageDtoBuilder jobId(String jobId) {
      this.jobId = jobId;
      return this;
    }

    private MeteringReferenceInfoMessageDto build() {
      return new MeteringReferenceInfoMessageDto(
        new MeterDto(
          physicalMeterId,
          medium,
          meterStatus,
          manufacturer,
          cron,
          revision,
          mbusDeviceType
        ),
        new FacilityDto(
          externalId,
          location.getCountry(),
          location.getCity(),
          location.getAddress()
        ),
        "Test source system",
        organisationExternalId,
        new GatewayStatusDto(
          gatewayExternalId,
          productModel,
          gatewayStatus
        ),
        jobId
      );
    }
  }
}
