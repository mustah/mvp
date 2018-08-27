package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Language;
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
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.geocode.MockGeocodeService;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMetersWithCascading;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockMeterStatusLogs;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockProperties;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
  private static final Integer READ_INTERVAL_IN_MINUTES = 15;
  private static final String HOT_WATER_MEDIUM = "Hot water";
  private static final String ADDRESS = "1234";
  private static final String ORGANISATION_EXTERNAL_ID = "Some Organisation";
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
  private ReferenceInfoMessageConsumer messageHandler;
  private MockGeocodeService geocodeService;
  private PropertiesUseCases propertiesUseCases;
  private MockMeterStatusLogs meterStatusLogs;

  @Before
  public void setUp() {
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .language(Language.en)
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
    messageHandler = new MeteringReferenceInfoMessageConsumer(
      new LogicalMeterUseCases(
        authenticatedUser,
        logicalMeters,
        new MockMeasurements()
      ),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters, meterStatusLogs),
      new OrganisationUseCases(
        authenticatedUser,
        organisations,
        new OrganisationPermissions(new MockUsers(singletonList(
          new UserBuilder()
            .name("super-admin")
            .email("super@admin.io")
            .password("password")
            .language(Language.en)
            .organisationElvaco()
            .asSuperAdmin()
            .build()
        )))
      ),
      new GatewayUseCases(gateways, authenticatedUser),
      geocodeService,
      propertiesUseCases
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get();

    PhysicalMeter savedPhysicalMeter = findPhysicalMeterByOrganisationId(organisation);

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      logicalMeter.id,
      EXTERNAL_ID,
      organisation.id,
      MeterDefinition.HOT_WATER_METER,
      logicalMeter.created,
      emptyList(),
      emptyList(),
      new LocationBuilder().country("Sweden").city("Kungsbacka").address("Kabelgatan 2T").build()
    );

    Gateway gateway = gateways.findBy(organisation.id, PRODUCT_MODEL, GATEWAY_EXTERNAL_ID).get();

    assertThat(logicalMeter).isEqualTo(expectedLogicalMeter);
    assertThat(savedPhysicalMeter).isEqualTo(new PhysicalMeter(
      savedPhysicalMeter.id,
      organisation,
      ADDRESS,
      EXTERNAL_ID,
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      logicalMeter.id,
      READ_INTERVAL_IN_MINUTES,
      savedPhysicalMeter.statuses
    ));
    assertThat(gateway.meters).extracting("id").containsExactly(logicalMeter.id);
  }

  @Test
  public void updatesExistingGatewayWithNewProductModel() {
    UUID gatewayId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(new Gateway(
      gatewayId,
      organisation.id,
      GATEWAY_EXTERNAL_ID,
      "OldValue"
    ));

    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

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
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      emptyList(),
      Location.UNKNOWN_LOCATION,
      null,
      0L,
      null
    ));

    Location newLocation = new LocationBuilder()
      .country("")
      .city("Växjö")
      .address("Gatvägen 41")
      .build();
    messageHandler.accept(newMessageWithLocation(newLocation));

    assertThat(logicalMeters.findById(meterId).get().location).isEqualTo(newLocation);
  }

  @Test
  public void createsOrganisationWithSameNameAsExternalId() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    assertThat(organisation.name).isEqualTo(ORGANISATION_EXTERNAL_ID);
  }

  @Test
  public void createsMeterAndGatewayForExistingOrganisation() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    Organisation organisation = findOrganisation();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).hasSize(1);
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
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    LogicalMeter saved = findLogicalMeter();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(saved.id);
  }

  @Test
  public void resendingSameMessageShouldNotUpdateExistingGateways() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    List<Gateway> allAfterFirstMessage = gateways.findAll(new MockRequestParameters());
    assertThat(allAfterFirstMessage).hasSize(1);

    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    assertThat(gateways.findAll(new MockRequestParameters())).isEqualTo(allAfterFirstMessage);
  }

  @Test
  public void gatewaysAreConnectedToMeters() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    List<Gateway> all = gateways.findAll(new MockRequestParameters());
    assertThat(all.stream().anyMatch(gateway -> gateway.meters.isEmpty())).isFalse();
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    messageHandler.accept(newMessageWithMedium("Unmappable medium"));

    List<LogicalMeter> meters = logicalMeters.findAllWithStatuses(new MockRequestParameters());
    assertThat(meters).hasSize(1);
    assertThat(meters.get(0).getMedium()).isEqualTo("Unknown medium");
  }

  @Test
  public void updatesMeterDefinitionForExistingLogicalMeter() {
    messageHandler.accept(newMessageWithMedium("Unknown medium"));

    LogicalMeter meter = logicalMeters.findAllWithStatuses(new MockRequestParameters()).get(0);
    assertThat(meter.getMedium()).isEqualTo("Unknown medium");

    messageHandler.accept(newMessageWithMedium("Heat, Return temp"));

    meter = logicalMeters.findAllWithStatuses(new MockRequestParameters()).get(0);
    assertThat(meter.meterDefinition.type).isEqualTo(MeterDefinition.DISTRICT_HEATING_METER.type);
  }

  @Test
  public void doesNotUpdateMeterDefinitionWithUnmappableMedium() {
    messageHandler.accept(newMessageWithMedium("Unknown medium"));

    LogicalMeter meter = logicalMeters.findAllWithStatuses(new MockRequestParameters()).get(0);
    assertThat(meter.getMedium()).isEqualTo("Unknown medium");

    messageHandler.accept(newMessageWithMedium("I don't even know what this is?"));

    meter = logicalMeters.findAllWithStatuses(new MockRequestParameters()).get(0);
    assertThat(meter.meterDefinition.type).isEqualTo(MeterDefinition.UNKNOWN_METER.type);
  }

  @Test
  public void updatesManufacturerForExistingMeter() {
    messageHandler.accept(newMessageWithManufacturer("ELV"));

    LogicalMeter meter = logicalMeters.findAllWithStatuses(new MockRequestParameters()).get(0);

    List<PhysicalMeter> all = physicalMeters.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).manufacturer).isEqualTo("ELV");
    assertThat(physicalMeters.findAll().stream().map(pm -> pm.logicalMeterId).collect(toList()))
      .containsExactly(meter.id);

    // Add same message with different manufacturer
    messageHandler.accept(newMessageWithManufacturer("KAM"));

    assertThat(physicalMeters.findAll().get(0).manufacturer).isEqualTo("KAM");
    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void callsGeocodeService() {
    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

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
  public void forceUpdateGeolocation_WhenFlagIsEnabledAndClearFlag() {
    UUID organisationId = saveDefaultOrganisation().getId();

    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    LocationBuilder builder = new LocationBuilder()
      .country("Sweden")
      .city("Kungsbacka")
      .address("Kabelgatan 2T")
      .id(geocodeService.requestId);

    assertThat(geocodeService.location).isEqualTo(builder.buildLocationWithId());

    propertiesUseCases.forceUpdateGeolocation(geocodeService.requestId, organisationId);

    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

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

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      EXTERNAL_ID,
      organisation.id,
      MeterDefinition.HOT_WATER_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION
    ));

    messageHandler.accept(newMessageWithMediumAndPhysicalMeterId(HOT_WATER_MEDIUM, "4321"));

    assertThat(logicalMeters.findAllByOrganisationId(organisation.id)).hasSize(1);
    assertThat(physicalMeters.findAll().stream().map(pm -> pm.logicalMeterId))
      .isEqualTo(asList(logicalMeterId, logicalMeterId));
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("An existing organisation"));
    physicalMeters.save(physicalMeter().organisation(organisation).build());

    messageHandler.accept(newMessageWithMedium(HOT_WATER_MEDIUM));

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void expectedIntervalIsSetForCreatedPhysicalMeter() {
    MeteringReferenceInfoMessageDto message = newMessageWithCron(HOUR_CRON);

    messageHandler.accept(message);

    PhysicalMeter createdMeter = physicalMeters.findAll().get(0);
    assertThat(createdMeter.readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void expectedIntervalIsUpdatedForCreatedPhysicalMeter() {
    messageHandler.accept(newMessageWithCron(FIFTEEN_MINUTE_CRON));
    messageHandler.accept(newMessageWithCron(HOUR_CRON));

    List<PhysicalMeter> all = physicalMeters.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void emptyExternalIdIsRejected() {
    MeteringReferenceInfoMessageDto message = newMessage(
      "medium",
      "manufacturer",
      "meter-id",
      FIFTEEN_MINUTE_CRON,
      UNKNOWN_LOCATION,
      ""
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void nullFacilityIdIsRejected() {
    MeteringReferenceInfoMessageDto message = new MeteringReferenceInfoMessageDto(
      null,
      new FacilityDto(null, null, null, null),
      "Test source system",
      "organisation id",
      null
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void physicalMeterRequiresMeterId() {
    MeteringReferenceInfoMessageDto message = new MeteringReferenceInfoMessageDto(
      new MeterDto(null, null, null, null, null),
      new FacilityDto("valid facility id", null, null, null),
      "Test source system",
      "organisation id",
      null
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).hasSize(1);
    assertThat(physicalMeters.findAll()).isEmpty();
  }

  @Test
  public void meterStatusIsSetForNewMeter() {
    messageHandler.accept(newMessageWithMeterStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
    assertThat(meterStatusLogs.allMocks()).extracting("status").containsExactly(StatusType.OK);
  }

  @Test
  public void sameMeterStatusIsUnchangedForMeter() {
    messageHandler.accept(newMessageWithMeterStatus(StatusType.OK));
    messageHandler.accept(newMessageWithMeterStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
    assertThat(meterStatusLogs.allMocks()).extracting("status").containsOnly(StatusType.OK);
  }

  @Test
  public void newMeterStatusChangesStatus() {
    messageHandler.accept(newMessageWithMeterStatus(StatusType.OK));
    messageHandler.accept(newMessageWithMeterStatus(StatusType.ERROR));

    List<StatusLogEntry<UUID>> statuses = physicalMeters.findAll().get(0).statuses;
    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  @Test
  public void newStatusIsSetForGateway() {
    messageHandler.accept(newMessageWithGatewayStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;

    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
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
      null
    );

    messageHandler.accept(message);

    assertThat(logicalMeters.findAllWithStatuses(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void sameGatewayStatusIsUnchangedForGateway() {
    messageHandler.accept(newMessageWithGatewayStatus(StatusType.OK));
    messageHandler.accept(newMessageWithGatewayStatus(StatusType.OK));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;
    assertThat(statuses).hasSize(1);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNull();
  }

  @Test
  public void newGatewayStatusChangesStatus() {
    messageHandler.accept(newMessageWithGatewayStatus(StatusType.OK));
    messageHandler.accept(newMessageWithGatewayStatus(StatusType.ERROR));

    List<StatusLogEntry<UUID>> statuses = gateways.findAll(new MockRequestParameters())
      .get(0).statusLogs;

    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).status).isEqualTo(StatusType.OK);
    assertThat(statuses.get(0).stop).isNotNull();

    assertThat(statuses.get(1).status).isEqualTo(StatusType.ERROR);
    assertThat(statuses.get(1).stop).isNull();
  }

  @Test
  public void createMeterWithoutCron_UseFallback() {
    messageHandler.accept(newMessageWithCron(null));

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(0);
  }

  @Test
  public void readIntervalShouldNotBeReset_WhenSecondMessageHasNoCron() {
    messageHandler.accept(newMessageWithCron(FIFTEEN_MINUTE_CRON));
    messageHandler.accept(newMessageWithCron(null));

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(15);
  }

  @Test
  public void readIntervalShouldNotBeReset_WhenSecondMessageHasEmptyCron() {
    messageHandler.accept(newMessageWithCron(FIFTEEN_MINUTE_CRON));
    messageHandler.accept(newMessageWithCron(""));

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(15);
  }

  @Test
  public void readIntervalShouldBeUpdated_WhenSecondMessageHasCron() {
    messageHandler.accept(newMessageWithCron(null));

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(0);

    messageHandler.accept(newMessageWithCron(HOUR_CRON));

    physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(60);
  }

  @Test
  public void readIntervalShouldUseFallback_WhenConsecutiveReadIntervalsAreMissing() {
    messageHandler.accept(newMessageWithCron(null));

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(0);

    messageHandler.accept(newMessageWithCron(null));

    physicalMeter = findPhysicalMeterByOrganisationId(organisation);

    assertThat(physicalMeter.readIntervalMinutes).isEqualTo(0);
  }

  @Test
  public void emptyGatewayField_MeterIsUpdatedGatewayIsNotCreated() {
    UUID meterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(new LogicalMeter(
      meterId,
      EXTERNAL_ID,
      organisation.id,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      emptyList(),
      Location.UNKNOWN_LOCATION,
      null,
      0L,
      null
    ));

    messageHandler.accept(
      newMessageWithLocation(new LocationBuilder().city("Borås").build())
        .withGatewayStatus(new GatewayStatusDto(null, null, null))
    );

    assertThat(logicalMeters.findById(meterId).get().location.getCity()).isEqualTo("borås");
    assertThat(gateways.findAll(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void emptyGatewayField_MeterIsCreatedGatewayIsNotCreated() {
    Organisation organisation = saveDefaultOrganisation();

    messageHandler.accept(
      newMessageWithLocation(new LocationBuilder().city("Borås").build())
        .withGatewayStatus(new GatewayStatusDto(null, null, null))
    );

    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get().location.getCity()).isEqualTo("borås");
    assertThat(gateways.findAll(new MockRequestParameters())).isEmpty();
  }

  @Test
  public void emptyMeterField_GatewayIsUpdatedAndMeterIsCreated() {
    Organisation organisation = saveDefaultOrganisation();
    UUID gatewayId = randomUUID();
    gateways.save(
      new Gateway(gatewayId, organisation.id, GATEWAY_EXTERNAL_ID, PRODUCT_MODEL)
    );

    messageHandler.accept(
      newMessageWithGatewayStatus(StatusType.CRITICAL)
        .withMeter(new MeterDto(null, null, null, null, null))
    );

    assertThat(gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get()
      .currentStatus().status).isEqualTo(StatusType.CRITICAL);
    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    )).isPresent();
  }

  @Test
  public void emptyMeterField_GatewayAndMeterIsCreated() {
    Organisation organisation = saveDefaultOrganisation();

    messageHandler.accept(
      newMessageWithGatewayStatus(StatusType.CRITICAL)
        .withMeter(new MeterDto(null, null, null, null, null))
    );

    assertThat(gateways.findBy(organisation.id, GATEWAY_EXTERNAL_ID).get()
      .currentStatus().status).isEqualTo(StatusType.CRITICAL);
    assertThat(logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    )).isPresent();
  }

  private PhysicalMeter findPhysicalMeterByOrganisationId(Organisation organisation) {
    return physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
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

  private MeteringReferenceInfoMessageDto newMessageWithGatewayStatus(
    StatusType gatewayStatus
  ) {
    return newMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      FIFTEEN_MINUTE_CRON,
      LOCATION_KUNGSBACKA,
      EXTERNAL_ID,
      StatusType.OK,
      gatewayStatus
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithMeterStatus(
    StatusType meterStatus
  ) {
    return newMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      FIFTEEN_MINUTE_CRON,
      LOCATION_KUNGSBACKA,
      EXTERNAL_ID,
      meterStatus,
      StatusType.OK
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithMediumAndPhysicalMeterId(
    String medium,
    String physicalMeterId
  ) {
    return newMessage(
      medium,
      "KAM",
      physicalMeterId,
      FIFTEEN_MINUTE_CRON,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithLocation(Location location) {
    return newMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      FIFTEEN_MINUTE_CRON,
      location
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithManufacturer(String manufacturer) {
    return newMessage(
      HOT_WATER_MEDIUM,
      manufacturer,
      ADDRESS,
      FIFTEEN_MINUTE_CRON,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithMedium(
    String medium
  ) {
    return newMessage(
      medium,
      MANUFACTURER,
      ADDRESS,
      FIFTEEN_MINUTE_CRON,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringReferenceInfoMessageDto newMessageWithCron(@Nullable String cron) {
    return newMessage(
      HOT_WATER_MEDIUM,
      MANUFACTURER,
      ADDRESS,
      cron,
      LOCATION_KUNGSBACKA
    );
  }

  private MeteringReferenceInfoMessageDto newMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    @Nullable String cron,
    Location location
  ) {
    return newMessage(
      medium,
      manufacturer,
      physicalMeterId,
      cron,
      location,
      EXTERNAL_ID
    );
  }

  private MeteringReferenceInfoMessageDto newMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    @Nullable String cron,
    Location location,
    String externalId
  ) {
    return newMessage(
      medium,
      manufacturer,
      physicalMeterId,
      cron,
      location,
      externalId,
      StatusType.OK,
      StatusType.OK
    );
  }

  private MeteringReferenceInfoMessageDto newMessage(
    String medium,
    String manufacturer,
    String physicalMeterId,
    @Nullable String cron,
    Location location,
    String externalId,
    StatusType meterStatus,
    StatusType gatewayStatus
  ) {
    return new MeteringReferenceInfoMessageDto(
      new MeterDto(physicalMeterId, medium, meterStatus.name(), manufacturer, cron),
      new FacilityDto(
        externalId,
        location.getCountry(),
        location.getCity(),
        location.getAddress()
      ),
      "Test source system",
      ORGANISATION_EXTERNAL_ID,
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
    return new Organisation(randomUUID(), name, code, name);
  }

  private static PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .address(ADDRESS)
      .externalId(EXTERNAL_ID)
      .medium(HOT_WATER_MEDIUM)
      .manufacturer(MANUFACTURER)
      .readIntervalMinutes(READ_INTERVAL_IN_MINUTES);
  }
}
