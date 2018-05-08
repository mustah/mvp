package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;
import com.elvaco.mvp.testdata.TestRabbitConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.toJson;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("SameParameterValue")
public class RabbitMqConsumerTest extends RabbitIntegrationTest {

  @Autowired
  private Organisations organisations;

  @Autowired
  private Gateways gateways;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @Before
  public void setUp() {
    assumeTrue(isRabbitConnected());
  }

  @After
  public void tearDown() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    organisations.findBySlug("some-organisation")
      .ifPresent(organisation -> organisations.deleteById(organisation.id));
    organisations.findBySlug("organisation-123")
      .ifPresent(organisation -> organisations.deleteById(organisation.id));
  }

  @Test
  public void messagesSentToRabbitAreReceivedAndProcessed() throws Exception {
    MeteringStructureMessageDto message = new MeteringStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      new MeterDto("1234", "Some medium", "OK", "A manufacturer", 15),
      new FacilityDto("facility-id", "Sweden", "Kungsbacka", "Kabelgatan 2T"),
      "test",
      "Some organisation",
      new GatewayStatusDto("123987", "Gateway 2000", "OK")
    );

    publishMessage(toJson(message).getBytes());

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisations.findBySlug("some-organisation").get().id;
    assertLogicalMeterWasCreated(organisationId, "facility-id");
    assertPhysicalMeterIsCreated(organisationId, "1234", "facility-id");
    assertGatewayWasCreated(organisationId, "123987");
  }

  @Test
  public void responseMessagesForMeasurementMessagesArePublished() throws Exception {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto("GATEWAY-123"),
      new MeterIdDto("METER-123"),
      new FacilityIdDto("FACILITY-123"),
      "ORGANISATION-123",
      "test",
      emptyList()
    );
    TestRabbitConsumer consumer = newResponseConsumer();

    publishMessage(toJson(message).getBytes());

    GetReferenceInfoDto response = consumer.fromJson(GetReferenceInfoDto.class);

    assertThat(response)
      .isEqualTo(
        new GetReferenceInfoDto(
          "ORGANISATION-123",
          "METER-123",
          "GATEWAY-123",
          "FACILITY-123"
        ));
  }

  private void assertLogicalMeterWasCreated(
    UUID organisationId,
    String externalId
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> logicalMeterJpaRepository.findBy(
      organisationId,
      externalId
    ).isPresent())).as("Logical meter '" + externalId + "' was created").isTrue();
  }

  private void assertGatewayWasCreated(
    UUID organisationId,
    String serial
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> gateways.findAllByOrganisationId(organisationId)
      .stream()
      .anyMatch(gateway -> gateway.serial.equals(serial))
    )).as("Gateway '" + serial + "' was created").isTrue();
  }

  private void assertPhysicalMeterIsCreated(
    UUID organisationId,
    String address,
    String externalId
  ) throws InterruptedException {
    assertThat(waitForCondition(
      () -> {
        Optional<PhysicalMeterEntity> meter = physicalMeterJpaRepository
          .findByOrganisationIdAndExternalIdAndAddress(
            organisationId,
            externalId,
            address
          );
        return meter.isPresent();
      }
    )).as("Physical meter '" + externalId + "' was created").isTrue();
  }

  private void assertOrganisationWithSlugWasCreated(String slug) throws InterruptedException {
    assertThat(waitForCondition(() -> organisations.findBySlug(slug)
      .isPresent())).as("Organisation '" + slug + "' was created").isTrue();
  }

}
