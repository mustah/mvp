package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.LocationDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class MessageHandlerTest {

  private static final String ORGANISATION_CODE = "some-organisation";
  private static final String EXTERNAL_ID = "ABC-123";

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Measurements measurements;
  private MessageHandler messageHandler;

  @Before
  public void setUp() {
    this.physicalMeters = new MockPhysicalMeters();
    this.organisations = new MockOrganisations();
    this.logicalMeters = new MockLogicalMeters(new ArrayList<>());
    this.measurements = new MockMeasurements();
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new User(
        randomUUID(),
        "mock user",
        "mock@somemail.nu",
        "P@$$w0rD",
        new Organisation(randomUUID(), "some organisation", ORGANISATION_CODE),
        singletonList(Role.USER)
      ),
      randomUUID().toString()
    );

    this.messageHandler = new MeteringMessageHandler(
      logicalMeters,
      physicalMeters,
      organisations,
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
      logicalMeter.id,
      emptyList()
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
    Organisation organisation = organisations.save(newOrganisation("An existing organisation"));
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
      Date.from(Instant.ofEpochMilli(123456L)),
      "Energy",
      1.0,
      "kWh",
      expectedPhysicalMeter
    );
    List<Measurement> createdMeasurements = measurements.findAll(emptyMap());
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
      Date.from(Instant.ofEpochMilli(123456L)),
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
    List<Measurement> createdMeasurements = measurements.findAll(emptyMap());
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
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
      new GatewayStatusDto("123", "Ok"),
      new MeterStatusDto("1234", "Ok"),
      EXTERNAL_ID,
      ORGANISATION_CODE,
      "Elvaco Metering",
      singletonList(new ValueDto(123456L, 1.0, "kWh", "Energy")),
      emptyList()
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(String medium, String manufacturer) {
    return new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      EXTERNAL_ID,
      medium,
      15,
      "Test source system",
      ORGANISATION_CODE,
      manufacturer,
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
  }

  private Organisation newOrganisation(String code) {
    return newOrganisation("", code);
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(randomUUID(), name, code);
  }
}
