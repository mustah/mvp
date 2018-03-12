package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MessageHandlerTest {

  private static final String ORGANISATION_CODE = "some-organisation";
  private static final String EXTERNAL_ID = "ABC-123";
  private static final ZonedDateTime EXPECTED_DATETIME = ZonedDateTime.of(
    2018,
    3,
    7,
    16,
    13,
    9,
    0,
    ZoneId
      .of("CET")
  );
  private static final long MEASUREMENT_TIMESTAMP = 1520435589000L;

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Measurements measurements;
  private MessageHandler messageHandler;

  @Before
  public void setUp() {
    this.physicalMeters = new MockPhysicalMeters();
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
        .organisation(new Organisation(randomUUID(), "some organisation", ORGANISATION_CODE))
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    OrganisationUseCases organisationUseCases = new OrganisationUseCases(
      authenticatedUser,
      organisations,
      new OrganisationPermissions(new MockUsers(singletonList(superAdmin)))
    );
    this.measurements = new MockMeasurements();
    this.logicalMeters = new MockLogicalMeters();

    this.messageHandler = new MeteringMessageHandler(
      new LogicalMeterUseCases(
        authenticatedUser,
        logicalMeters,
        measurements
      ),
      new PhysicalMeterUseCases(authenticatedUser, physicalMeters),
      organisationUseCases,
      new MeasurementUseCases(authenticatedUser, this.measurements)
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    messageHandler.handle(newStructureMessage("Hot water", "ELV"));

    Organisation organisation = findOrganisation();

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      EXTERNAL_ID
    ).get();

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      logicalMeter.id,
      EXTERNAL_ID,
      organisation.id,
      Location.UNKNOWN_LOCATION,
      logicalMeter.created,
      emptyList(),
      MeterDefinition.HOT_WATER_METER,
      emptyList(),
      emptyList()
    );

    PhysicalMeter savedPhysicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      EXTERNAL_ID,
      "1234"
    ).get();

    assertThat(logicalMeter).isEqualTo(expectedLogicalMeter);
    assertThat(savedPhysicalMeter).isEqualTo(new PhysicalMeter(
      savedPhysicalMeter.id,
      organisation,
      "1234",
      EXTERNAL_ID,
      "Hot water",
      "ELV",
      logicalMeter.id
    ));
  }

  @Test
  public void createsMeterForExistingOrganisation() {
    messageHandler.handle(newStructureMessage("Hot water", "ELV"));

    Organisation expectedOrganisation = findOrganisation();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(logicalMeters.findAll()).hasSize(1);
    assertThat(organisations.findAll()).hasSize(1);
    PhysicalMeter meter = allPhysicalMeters.get(0);
    assertThat(meter.organisation).isEqualTo(expectedOrganisation);

    LogicalMeter logicalMeter = logicalMeters.findById(meter.logicalMeterId).get();
    assertThat(logicalMeter.meterDefinition).isEqualTo(MeterDefinition.HOT_WATER_METER);
  }

  @Test
  public void addsPhysicalMeterToExistingLogicalMeter() {
    messageHandler.handle(newStructureMessage("Hot water", "ELV"));

    LogicalMeter saved = findLogicalMeter();

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(saved.id);
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    messageHandler.handle(newStructureMessage("Unmappable medium", "ELV"));

    List<LogicalMeter> meters = logicalMeters.findAll();
    assertThat(meters).hasSize(1);
    assertThat(meters.get(0).getMedium()).isEqualTo("Unknown meter");
  }

  @Test
  @Ignore("Does this really happen? An identical meter with a new manufacturer/medium really "
    + "ought to be considered a new physical meter.")
  public void updatesExistingMeterForExistingOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "KAM");
    Organisation organisation = organisations.save(
      newOrganisation("An existing organisation", "Some organisation")
    );
    UUID physicalMeterId = randomUUID();

    physicalMeters.save(new PhysicalMeter(
      physicalMeterId,
      "1234",
      EXTERNAL_ID,
      "Hot water",
      "ELV",
      organisation
    ));

    messageHandler.handle(structureMessage);

    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      physicalMeterId,
      organisation,
      "1234",
      EXTERNAL_ID,
      "Hot water",
      "KAM"
    );
    assertThat(physicalMeters.findAll()).containsExactly(expectedPhysicalMeter);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("An existing "
                                                                     + "organisation"));
    physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      "1234",
      EXTERNAL_ID,
      "Hot water",
      "ELV",
      organisation
    ));

    messageHandler.handle(newStructureMessage("Hot water", "ELV"));

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    Organisation organisation = organisations.save(newOrganisation(ORGANISATION_CODE));

    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      new PhysicalMeter(
        randomUUID(),
        "1234",
        EXTERNAL_ID,
        "Electricity",
        "ELV",
        organisation
      )
    );

    messageHandler.handle(newMeasurementMessage());

    Measurement expectedMeasurement = new Measurement(
      1L,
      EXPECTED_DATETIME,
      "Energy",
      1.0,
      "kWh",
      expectedPhysicalMeter
    );
    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  @Test
  public void createsOrganisationAndMeterForMeasurement() {
    messageHandler.handle(newMeasurementMessage());

    Organisation organisation = findOrganisation();

    PhysicalMeter physicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      EXTERNAL_ID,
      "1234"
    ).get();

    Measurement expectedMeasurement = new Measurement(
      1L,
      EXPECTED_DATETIME,
      "Energy",
      1.0,
      "kWh",
      new PhysicalMeter(
        physicalMeter.id,
        organisation,
        "1234",
        EXTERNAL_ID,
        "Unknown",
        "UNKNOWN"
      )
    );
    List<Measurement> createdMeasurements = measurements.findAll(null);
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  @Test
  public void ignoresAlarmsWithoutCrashing() {
    messageHandler.handle(newAlarmMessageWithoutAlarms());
    messageHandler.handle(newAlarmMessageWithTwoAlarms());
  }

  private LogicalMeter findLogicalMeter() {
    Organisation organisation = findOrganisation();
    return logicalMeters.findByOrganisationIdAndExternalId(organisation.id, EXTERNAL_ID).get();
  }

  private Organisation findOrganisation() {
    return organisations.findByCode(ORGANISATION_CODE).get();
  }

  private MeteringMeasurementMessageDto newMeasurementMessage() {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto("123"),
      new MeterIdDto("1234"),
      new FacilityIdDto(EXTERNAL_ID),
      ORGANISATION_CODE,
      "Elvaco Metering",
      singletonList(new ValueDto(MEASUREMENT_TIMESTAMP, 1.0, "kWh", "Energy"))
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(String medium, String manufacturer) {
    return new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      new MeterDto("1234", medium, "OK", manufacturer, 15),
      new FacilityDto(EXTERNAL_ID, "Sweden", "Kungsbacka", "Kabelgatan 2T"),
      "Test source system",
      ORGANISATION_CODE,
      new GatewayStatusDto("gateway-id", "CMi2110", "OK")
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
      Collections.emptyList()
    );
  }

  private MeteringAlarmMessageDto newAlarmMessageWithTwoAlarms() {
    List<AlarmDto> alarms = new ArrayList<>();
    alarms.add(new AlarmDto(1234, 88));
    alarms.add(new AlarmDto(1235, 99));
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

  private Organisation newOrganisation(String code) {
    return newOrganisation("", code);
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(randomUUID(), name, code);
  }
}
