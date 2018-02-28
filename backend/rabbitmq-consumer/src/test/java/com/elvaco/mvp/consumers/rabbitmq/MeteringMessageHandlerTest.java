package com.elvaco.mvp.consumers.rabbitmq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.message.GatewayDto;
import com.elvaco.mvp.consumers.rabbitmq.message.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.message.LocationDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.message.MeterStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.ValueDto;
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
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageHandlerTest {

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private Measurements measurements;
  private MeteringMessageHandler messageHandler;

  @Before
  public void setUp() {
    this.physicalMeters = new MockPhysicalMeters();
    this.organisations = new MockOrganisations();
    this.logicalMeters = new MockLogicalMeters(new ArrayList<>());
    this.measurements = new MockMeasurements();
    AuthenticatedUser authenticatedUser = new MockAuthenticatedUser(
      new User(
        0L,
        "mock user",
        "mock@somemail.nu",
        "P@$$w0rD",
        new Organisation(0L, "some organisation", "some-organisation"),
        singletonList(Role.USER)
      ),
      UUID.randomUUID().toString()
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
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "ELV");

    messageHandler.handle(structureMessage);

    Organisation expectedOrganisation = new Organisation(1L, "", "Some organisation");
    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      1L,
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Hot water",
      "ELV",
      1L,
      emptyList()
    );

    LogicalMeter actualLogicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      expectedOrganisation.id,
      "ABC-123"
    ).get();

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      actualLogicalMeter.id,
      "ABC-123",
      expectedOrganisation.id,
      Location.UNKNOWN_LOCATION,
      actualLogicalMeter.created,
      emptyList(),
      MeterDefinition.HOT_WATER_METER,
      emptyList(),
      emptyList()
    );

    assertThat(actualLogicalMeter).isEqualTo(expectedLogicalMeter);

    assertThat(
      physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
        expectedOrganisation.id,
        "ABC-123",
        "1234"
      ).get()
    ).isEqualTo(expectedPhysicalMeter);
  }

  @Test
  public void createsMeterForExistingOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "ELV");
    Organisation expectedOrganisation = organisations.save(
      newOrganisation("An existing organisation", "Some organisation")
    );

    messageHandler.handle(structureMessage);

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
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "ELV");
    LogicalMeter expectedLogicalMeter = logicalMeters.save(
      new LogicalMeter(
        1L,
        "ABC-123",
        0L,
        Location.UNKNOWN_LOCATION,
        new Date()
      )
    );

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(expectedLogicalMeter.id);
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage(
      "Unmappable medium",
      "ELV"
    );

    messageHandler.handle(structureMessage);

    LogicalMeter unmappableMeter = logicalMeters.findById(1L).get();
    assertThat(unmappableMeter.getMedium()).isEqualTo("Unknown meter");
  }

  @Test
  @Ignore("Does this really happen? An identical meter with a new manufacturer/medium really "
    + "ought to be considered a new physical meter.")
  public void updatesExistingMeterForExistingOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "KAM");
    Organisation expectedOrganisation = organisations.save(
      newOrganisation("An existing organisation", "Some organisation")
    );
    Long physicalMeterId = physicalMeters.save(new PhysicalMeter(
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Hot water",
      "ELV"
    )).id;

    messageHandler.handle(structureMessage);

    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      physicalMeterId,
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Hot water",
      "KAM"
    );
    assertThat(physicalMeters.findAll()).containsExactly(expectedPhysicalMeter);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "ELV");
    Organisation organisation = organisations.save(
      newOrganisation("An existing organisation")
    );
    physicalMeters.save(new PhysicalMeter(organisation, "1234", "ABC-123", "Hot water", "ELV"));

    messageHandler.handle(structureMessage);

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage();
    Organisation expectedOrganisation = organisations.save(newOrganisation("some-organisation"));
    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      new PhysicalMeter(
        expectedOrganisation,
        "1234",
        "ABC-123",
        "Electricity",
        "ELV"
      )
    );

    messageHandler.handle(measurementMessage);

    Measurement expectedMeasurement = new Measurement(
      0L,
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
  public void createsMeterForExistingOrganisationForMeasurement() {
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage();
    Organisation expectedOrganisation = organisations.save(newOrganisation("some-organisation"));

    messageHandler.handle(measurementMessage);

    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      0L,
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Unknown",
      "UNKNOWN"
    );
    Measurement expectedMeasurement = new Measurement(
      0L,
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
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage();

    messageHandler.handle(measurementMessage);

    Organisation expectedOrganisation = new Organisation(0L, "", "some-organisation");
    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      0L,
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Unknown",
      "UNKNOWN"
    );
    Measurement expectedMeasurement = new Measurement(
      0L,
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

  private MeteringMeasurementMessageDto newMeasurementMessage() {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayStatusDto("123", "Ok"),
      new MeterStatusDto("1234", "Ok"),
      "ABC-123",
      "some-organisation",
      "Elvaco Metering",
      singletonList(new ValueDto(123456L, 1.0, "kWh", "Energy")),
      emptyList()
    );
  }

  private MeteringMeterStructureMessageDto newStructureMessage(String medium, String manufacturer) {
    return new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      medium,
      15,
      "Test source system",
      "Some organisation",
      manufacturer,
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
  }

  private Organisation newOrganisation(String code) {
    return newOrganisation("", code);
  }

  private Organisation newOrganisation(String name, String code) {
    return new Organisation(null, name, code);
  }
}
