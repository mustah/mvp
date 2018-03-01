package com.elvaco.mvp.consumers.rabbitmq;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageHandlerTest {

  private static final String ORGANISATION_CODE = "some-organisation";

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

    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      1L,
      organisation,
      "1234",
      "ABC-123",
      "Hot water",
      "ELV",
      1L,
      emptyList()
    );

    LogicalMeter actualLogicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      "ABC-123"
    ).get();

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      actualLogicalMeter.id,
      "ABC-123",
      organisation.id,
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
        organisation.id,
        "ABC-123",
        "1234"
      ).get()
    ).isEqualTo(expectedPhysicalMeter);
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

    LogicalMeter expectedLogicalMeter = logicalMeters.save(
      new LogicalMeter(
        1L,
        "ABC-123",
        randomUUID(),
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
    messageHandler.handle(newStructureMessage("Unmappable medium", "ELV"));

    LogicalMeter unmappableMeter = logicalMeters.findById(1L).get();
    assertThat(unmappableMeter.getMedium()).isEqualTo("Unknown meter");
  }

  @Test
  @Ignore("Does this really happen? An identical meter with a new manufacturer/medium really "
          + "ought to be considered a new physical meter.")
  public void updatesExistingMeterForExistingOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = newStructureMessage("Hot water", "KAM");
    Organisation organisation = organisations.save(
      newOrganisation("An existing organisation", "Some organisation")
    );
    Long physicalMeterId = physicalMeters.save(new PhysicalMeter(
      organisation,
      "1234",
      "ABC-123",
      "Hot water",
      "ELV"
    )).id;

    messageHandler.handle(structureMessage);

    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      physicalMeterId,
      organisation,
      "1234",
      "ABC-123",
      "Hot water",
      "KAM"
    );
    assertThat(physicalMeters.findAll()).containsExactly(expectedPhysicalMeter);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    Organisation organisation = organisations.save(newOrganisation("An existing organisation"));
    physicalMeters.save(new PhysicalMeter(organisation, "1234", "ABC-123", "Hot water", "ELV"));

    messageHandler.handle(newStructureMessage("Hot water", "ELV"));

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }

  @Test
  public void addsMeasurementToExistingMeter() {
    Organisation organisation = organisations.save(newOrganisation(ORGANISATION_CODE));

    PhysicalMeter expectedPhysicalMeter = physicalMeters.save(
      new PhysicalMeter(
        organisation,
        "1234",
        "ABC-123",
        "Electricity",
        "ELV"
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

    Measurement expectedMeasurement = new Measurement(
      1L,
      Date.from(Instant.ofEpochMilli(123456L)),
      "Energy",
      1.0,
      "kWh",
      new PhysicalMeter(
        1L,
        organisation,
        "1234",
        "ABC-123",
        "Unknown",
        "UNKNOWN"
      )
    );
    List<Measurement> createdMeasurements = measurements.findAll(emptyMap());
    assertThat(createdMeasurements).hasSize(1);
    assertThat(createdMeasurements.get(0)).isEqualTo(expectedMeasurement);
  }

  private Organisation findOrganisation() {
    return organisations.findByCode(ORGANISATION_CODE).get();
  }

  private MeteringMeasurementMessageDto newMeasurementMessage() {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayStatusDto("123", "Ok"),
      new MeterStatusDto("1234", "Ok"),
      "ABC-123",
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
      "ABC-123",
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
