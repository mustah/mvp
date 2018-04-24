package com.elvaco.mvp.consumers.rabbitmq;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.elvaco.mvp.configuration.config.RabbitConsumerProperties;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageSerializer;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.consumers.rabbitmq.message.MessageSerializer.toJson;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;

@SuppressWarnings("SameParameterValue")
public class RabbitMqConsumerTest extends IntegrationTest {

  @Autowired
  private Organisations organisations;

  @Autowired
  private Gateways gateways;

  @Autowired
  private ConnectionFactory connectionFactory;

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

  @Autowired
  private RabbitConsumerProperties consumerProperties;

  private Connection connection;
  private Channel channel;

  @Before
  public void setUp() {
    try {
      connection = connectionFactory.createConnection();
      channel = connection.createChannel(false);
    } catch (AmqpConnectException ex) {
      if (connection != null) {
        connection.close();
      }
      assumeNoException(ex);
    }
  }

  @After
  public void tearDown() throws IOException, TimeoutException {
    if (channel != null) {
      channel.close();
    }
    if (connection != null) {
      connection.close();
    }

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
    TestConsumer consumer = newResponseConsumer();

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

  private TestConsumer newResponseConsumer() throws IOException {
    TestConsumer consumer = new TestConsumer(new LinkedBlockingQueue<>());
    channel.queueDeclare(
      consumerProperties.getResponseRoutingKey(),
      false,
      true,
      true,
      emptyMap()
    );
    channel.basicConsume(consumerProperties.getResponseRoutingKey(), consumer);
    return consumer;
  }

  private void publishMessage(byte[] message) throws IOException {
    channel.basicPublish("", consumerProperties.getQueueName(), null, message);
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

  private class TestConsumer extends DefaultConsumer {

    private final BlockingQueue<Object> receivedMessages;

    private TestConsumer(BlockingQueue<Object> receivedMessages) {
      super(channel);
      this.receivedMessages = receivedMessages;
    }

    @Override
    public void handleDelivery(
      String consumerTag,
      Envelope envelope,
      AMQP.BasicProperties properties,
      byte[] body
    ) {
      receivedMessages.add(body);
    }

    private byte[] receiveOne() throws InterruptedException {
      return (byte[]) receivedMessages.poll(10, TimeUnit.SECONDS);
    }

    private <T> T fromJson(Class<T> classOfT) throws InterruptedException {
      return MessageSerializer.fromJson(new String(receiveOne()), classOfT);
    }
  }
}
