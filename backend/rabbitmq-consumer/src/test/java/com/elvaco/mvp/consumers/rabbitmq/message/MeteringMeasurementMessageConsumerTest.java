package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageConsumer.METERING_TIMEZONE;
import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MeteringMeasurementMessageConsumerTest {

  private static final String QUANTITY = "Energy";
  private static final String GATEWAY_EXTERNAL_ID = "123";
  private static final String ADDRESS = "1234";
  private static final String ORGANISATION_SLUG = "some-organisation";
  private static final String EXTERNAL_ID = "ABC-123";
  private static final ZonedDateTime CREATED_DATE_TIME =
    ZonedDateTime.of(LocalDateTime.parse("2018-03-07T16:13:09"), METERING_TIMEZONE);
  private static final LocalDateTime MEASUREMENT_TIMESTAMP =
    LocalDateTime.parse("2018-03-07T16:13:09");

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Gateways gateways;
  private MockMeasurements measurements;
  private MeteringMeasurementMessageConsumer messageConsumer;

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
    measurements = new MockMeasurements();
    logicalMeters = new MockLogicalMeters();
    gateways = new MockGateways();

    messageConsumer = new MeteringMeasurementMessageConsumer(
      new LogicalMeterUseCases(
        authenticatedUser,
        logicalMeters,
        measurements
      ),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters),
      new OrganisationUseCases(
        authenticatedUser,
        organisations,
        new OrganisationPermissions(new MockUsers(singletonList(superAdmin)))
      ),
      new MeasurementUseCases(authenticatedUser, measurements),
      new GatewayUseCases(gateways, authenticatedUser)
    );
  }

  @Test
  public void survivesMissingGatewayFieldInMeasurementMessage() {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      null,
      new MeterIdDto(EXTERNAL_ID),
      new FacilityIdDto(EXTERNAL_ID),
      ORGANISATION_SLUG,
      "Test source system",
      emptyList()
    );

    messageConsumer.accept(message);

    assertThat(gateways.findAll(null)).isEmpty();
    assertThat(organisations.findAll()).hasSize(1);
    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAll(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void survivesEmptyGatewayFieldInMeasurementMessage() {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto(null),
      new MeterIdDto(EXTERNAL_ID),
      new FacilityIdDto(EXTERNAL_ID),
      ORGANISATION_SLUG,
      "Test source system",
      emptyList()
    );

    messageConsumer.accept(message);

    assertThat(gateways.findAll(null)).isEmpty();
    assertThat(organisations.findAll()).hasSize(1);
    assertThat(physicalMeters.findAll()).hasSize(1);
    assertThat(logicalMeters.findAll(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void measurementIsUpdated() {
    UUID meterId = randomUUID();
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(new LogicalMeter(
      meterId,
      EXTERNAL_ID,
      organisation.id,
      UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    messageConsumer.accept(newMeasurementMessage("Wattage", "W", 1.0));
    messageConsumer.accept(newMeasurementMessage("Wattage", "W", 2.0));

    List<Measurement> actual = measurements.allMocks();
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).value).isEqualTo(2.0);
  }

  @Test
  public void measurementIsMappedToMvpMeasurements() {
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      EXTERNAL_ID,
      organisation.id,
      UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto(GATEWAY_EXTERNAL_ID),
      new MeterIdDto(ADDRESS),
      new FacilityIdDto(EXTERNAL_ID),
      organisation.externalId,
      "Elvaco Metering",
      asList(
        new ValueDto(LocalDateTime.parse("2018-03-16T13:07:01"), 35.0, "°C", "Return temp."),
        new ValueDto(LocalDateTime.parse("2018-03-16T14:07:01"), 36.7, "°C", "Return temp.")
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
  public void measurementForNewQuantityIsNotUpdated() {
    Organisation organisation = saveDefaultOrganisation();
    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      EXTERNAL_ID,
      organisation.id,
      UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    messageConsumer.accept(newMeasurementMessage("Wattage", "W", 1.0));
    messageConsumer.accept(newMeasurementMessage("Flow", "m³/s", 2.0));

    assertThat(measurements.allMocks()).hasSize(2);
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    Organisation organisation = saveDefaultOrganisation();

    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      newPhysicalMeter(organisation, "Electricity")
    );

    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    Measurement expectedMeasurement = new Measurement(
      1L,
      CREATED_DATE_TIME,
      QUANTITY,
      1.0,
      "kWh",
      expectedPhysicalMeter
    );
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

    PhysicalMeter physicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
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
    assertThat(createdMeasurements.get(0)).isEqualTo(new Measurement(
      1L,
      CREATED_DATE_TIME,
      QUANTITY,
      1.0,
      "kWh",
      new PhysicalMeter(
        physicalMeter.id,
        organisation,
        ADDRESS,
        EXTERNAL_ID,
        "Unknown medium",
        "UNKNOWN",
        0
      ).withLogicalMeterId(logicalMeter.id)
    ));
  }

  @Test
  public void usesOrganisationExternalIdForMeasurementMessage() {
    Organisation existingOrganisation = new Organisation(
      randomUUID(),
      "An organisation",
      "an-organisation",
      "An external organisation ID"
    );
    organisations.save(existingOrganisation);

    messageConsumer.accept(newMeasurementMessage("An external organisation ID"));

    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements.get(0).physicalMeter.organisation)
      .isEqualTo(existingOrganisation);
  }

  @Test
  public void createdOrganisationIsSlugged() {
    MeteringMeasurementMessageDto message = newMeasurementMessage("Some Organisation");

    messageConsumer.accept(message);

    List<Measurement> createdMeasurements = measurements.allMocks();
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0).physicalMeter.organisation.slug)
      .isEqualTo("some-organisation");
  }

  @Test
  public void gatewayIsCreatedFromMeasurementMessage() {
    MeteringMeasurementMessageDto message = measurementMessageWithUnit("kWh");

    messageConsumer.accept(message);

    assertThat(gateways.findAll(new MockRequestParameters())).hasSize(1);
  }

  @Test
  public void measurementUnitIsUpdated() {
    messageConsumer.accept(measurementMessageWithUnit("kW"));

    List<Measurement> all = measurements.allMocks();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("kW");
    assertThat(all.get(0).value).isEqualTo(1.0);

    messageConsumer.accept(measurementMessageWithUnit("MW"));

    all = measurements.allMocks();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).unit).isEqualTo("MW");
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
  public void measurementValueForMissingLogicalMeter_CreatesNewLogicalMeter() {
    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.facilityId).isEqualTo("ABC-123");
    assertThat(response.meterExternalId).isEqualTo("1234");
  }

  @Test
  public void measurementValueForMissingPhysicalMeter_CreatesNewPhysicalMeter() {
    Organisation organisation = saveDefaultOrganisation();

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      EXTERNAL_ID,
      organisation.id,
      MeterDefinition.HOT_WATER_METER,
      UNKNOWN_LOCATION
    ));

    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.facilityId).isEqualTo("ABC-123");
    assertThat(response.meterExternalId).isEqualTo("1234");
    assertThat(response.gatewayExternalId).isEqualTo("123");
  }

  @Test
  public void measurementValueForExistingGateway_DoesNotCreateNewGateway() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));

    GetReferenceInfoDto response = messageConsumer.accept(measurementMessageWithUnit("kWh")).get();

    assertThat(response.gatewayExternalId).isNull();
    assertThat(response.facilityId).isNotNull();
    assertThat(response.meterExternalId).isNotNull();
  }

  @Test
  public void measurementValueForExistingGatewayDoesNotModifyGateway() {
    Organisation organisation = saveDefaultOrganisation();
    Gateway existingGateway = gateways.save(newGateway(organisation.id));

    messageConsumer.accept(measurementMessageWithUnit("kWh"));

    List<Gateway> all = gateways.findAll(null);
    assertThat(all).hasSize(1);
    assertThat(all.get(0)).isEqualTo(existingGateway);
  }

  @Test
  public void measurementValueForExistingEntities_CreateNoNewEntities() {
    Organisation organisation = saveDefaultOrganisation();
    gateways.save(newGateway(organisation.id));
    physicalMeters.save(newPhysicalMeter(organisation, "Hot water"));
    logicalMeters.save(
      new LogicalMeter(
        randomUUID(),
        EXTERNAL_ID,
        organisation.id,
        MeterDefinition.HOT_WATER_METER,
        UNKNOWN_LOCATION
      )
    );

    Optional<GetReferenceInfoDto> response =
      messageConsumer.accept(measurementMessageWithUnit("kWh"));

    assertThat(response.isPresent()).isFalse();
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(newOrganisation());
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
      "ELV",
      organisation,
      15
    );
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(double value) {
    return newMeasurementMessage(
      ORGANISATION_SLUG,
      QUANTITY,
      "kWh",
      value
    );
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(String organisationExternalId) {
    return newMeasurementMessage(organisationExternalId, QUANTITY, "kWh", 1.0);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(
    String quantity,
    String unit,
    double value
  ) {
    return newMeasurementMessage(ORGANISATION_SLUG, quantity, unit, value);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(
    String organisationExternalId,
    String quantity,
    String unit,
    double value
  ) {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto(GATEWAY_EXTERNAL_ID),
      new MeterIdDto(ADDRESS),
      new FacilityIdDto(EXTERNAL_ID),
      organisationExternalId,
      "Elvaco Metering",
      singletonList(new ValueDto(MEASUREMENT_TIMESTAMP, value, unit, quantity))
    );
  }

  private MeteringMeasurementMessageDto measurementMessageWithUnit(String unit) {
    return newMeasurementMessage(ORGANISATION_SLUG, QUANTITY, unit, 1.0);
  }

  private Gateway newGateway(UUID organisationId) {
    return new Gateway(
      randomUUID(),
      organisationId,
      GATEWAY_EXTERNAL_ID,
      "CMi2110"
    );
  }

  private Organisation newOrganisation() {
    return new Organisation(randomUUID(), "", ORGANISATION_SLUG);
  }
}