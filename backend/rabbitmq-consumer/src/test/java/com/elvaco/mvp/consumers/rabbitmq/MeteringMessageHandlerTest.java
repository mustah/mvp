package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Collections;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.message.GatewayDto;
import com.elvaco.mvp.consumers.rabbitmq.message.LocationDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageHandlerTest {

  private PhysicalMeters physicalMeters;
  private Organisations organisations;
  private MeteringMessageHandler messageHandler;

  @Before
  public void setUp() {
    this.physicalMeters = new MockPhysicalMeters();
    this.organisations = new MockOrganisations();
    messageHandler = new MeteringMessageHandler(
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
      null,
      Collections.emptyList()
    );
    List<PhysicalMeter> allPhysicalMeters = physicalMeters.findAll();
    assertThat(allPhysicalMeters).hasSize(1);
    PhysicalMeter meter = allPhysicalMeters.get(0);
    assertThat(meter.organisation).isEqualTo(expectedOrganisation);
    assertThat(meter).isEqualTo(expectedPhysicalMeter);
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
    assertThat(organisations.findAll()).hasSize(1);
    PhysicalMeter meter = allPhysicalMeters.get(0);
    assertThat(meter.organisation).isEqualTo(expectedOrganisation);
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
