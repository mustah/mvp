package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.message.GatewayDto;
import com.elvaco.mvp.consumers.rabbitmq.message.LocationDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageHandlerTest {

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private LogicalMeters logicalMeters;
  private MeteringMessageHandler messageHandler;

  @Before
  public void setUp() {
    this.physicalMeters = new MockPhysicalMeters();
    this.organisations = new MockOrganisations();
    this.logicalMeters = new MockLogicalMeters();

    this.messageHandler = new MeteringMessageHandler(
      logicalMeters,
      physicalMeters,
      organisations
    );
  }

  @Test
  public void createsMeterAndOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Hot water",
      15,
      "Test source system",
      "Some organisation",
      "ELV",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );

    messageHandler.handle(structureMessage);

    Organisation expectedOrganisation = new Organisation(0L, "", "Some organisation");
    PhysicalMeter expectedPhysicalMeter = new PhysicalMeter(
      0L,
      expectedOrganisation,
      "1234",
      "ABC-123",
      "Hot water",
      "ELV",
      0L,
      Collections.emptyList()
    );

    LogicalMeter actualLogicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      expectedOrganisation.id,
      "ABC-123"
    ).orElse(null);

    LogicalMeter expectedLogicalMeter = new LogicalMeter(
      0L,
      "ABC-123",
      expectedOrganisation.id,
      Location.UNKNOWN_LOCATION,
      actualLogicalMeter.created,
      Collections.emptyList(),
      MeterDefinition.HOT_WATER_METER,
      Collections.emptyList()
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
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Hot water",
      15,
      "Test source system",
      "Some organisation",
      "ELV",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
    Organisation expectedOrganisation = organisations.save(
      new Organisation(null, "An existing organisation", "Some organisation")
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
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Hot water",
      15,
      "Test source system",
      "Some organisation",
      "ELV",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
    LogicalMeter expectedLogicalMeter = logicalMeters.save(
      new LogicalMeter(
        null,
        "ABC-123", 0L, Location.UNKNOWN_LOCATION,
        new Date()
      )
    );

    messageHandler.handle(structureMessage);

    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    assertThat(allPhysicalMeters.get(0).logicalMeterId).isEqualTo(expectedLogicalMeter.id);
  }

  @Test
  public void setsNoMeterDefinitionForUnmappableMedium() {
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Unmappable medium",
      15,
      "Test source system",
      "Some organisation",
      "ELV",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );

    messageHandler.handle(structureMessage);

    assertThat(logicalMeters.findById(0L).get().getMedium()).isEqualTo("Unknown meter");
  }

  @Test
  public void updatesExistingMeterForExistingOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Hot water",
      15,
      "Test source system",
      "Some organisation",
      "KAM",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
    Organisation expectedOrganisation = organisations.save(
      new Organisation(null, "An existing organisation", "Some organisation")
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
    assertThat(organisations.findAll()).hasSize(1);
  }

  @Test
  public void duplicateIdentityAndExternalIdentityForOtherOrganisation() {
    MeteringMeterStructureMessageDto structureMessage = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "ABC-123",
      "Hot water",
      15,
      "Test source system",
      "Some organisation",
      "ELV",
      new GatewayDto("gateway-id", "CMi2110"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );
    Organisation organisation = organisations.save(
      new Organisation(null, "An existing organisation", "Some other organisation")
    );
    physicalMeters.save(new PhysicalMeter(organisation, "1234", "ABC-123", "Hot water", "ELV"));

    messageHandler.handle(structureMessage);

    assertThat(organisations.findAll()).hasSize(2);
    assertThat(physicalMeters.findAll()).hasSize(2);
  }
}
