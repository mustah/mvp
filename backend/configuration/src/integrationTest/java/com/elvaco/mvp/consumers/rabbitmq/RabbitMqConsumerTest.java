package com.elvaco.mvp.consumers.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.LocationDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageSerializer;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.rabbitmq.client.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;

public class RabbitMqConsumerTest extends IntegrationTest {

  @Autowired
  Organisations organisations;
  @Autowired
  PhysicalMeters physicalMeters;
  @Autowired
  RabbitProperties rabbitProperties;
  @Autowired
  ConnectionFactory connectionFactory;

  @Autowired
  PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  LogicalMeters logicalMeters;

  @Value("${mvp.consumers.rabbit.queueName}")
  private String queueName;
  private Connection connection;
  private Channel channel;

  @Before
  public void setUp() {
    try {
      connection = connectionFactory.createConnection();
      channel = connection.createChannel(false);
    } catch (AmqpConnectException ex) {
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

    physicalMeterJpaRepository.deleteAll();
    logicalMeters.deleteAll();
    organisations.findByCode("Some organisation")
      .ifPresent(organisation -> organisations.deleteById(organisation.id));
  }

  @Test
  public void messagesSentToRabbitAreReceivedAndProcessed() throws InterruptedException,
    IOException {
    MeteringMeterStructureMessageDto messageDto = new MeteringMeterStructureMessageDto(
      MessageType.METERING_METER_STRUCTURE_V_1_0,
      "1234",
      "facility-id",
      "Some medium",
      15,
      "test",
      "Some organisation",
      "TEST",
      new GatewayDto("GW-1234", "Gateway 2000"),
      new LocationDto("Sweden", "Kungsbacka", "Kabelgatan 2T")
    );

    publishMessage(serializeDto(messageDto));
    assertOrganisationWithCodeWasCreated("Some organisation");
  }

  private byte[] serializeDto(MeteringMessageDto dto) {
    return new MeteringMessageSerializer().serialize(dto).getBytes();
  }

  private void publishMessage(byte[] message) throws IOException {
    channel.basicPublish("", queueName, null, message);
  }

  private void assertOrganisationWithCodeWasCreated(String code) throws InterruptedException {
    assertThat(waitForCondition(() -> organisations.findByCode(code)
      .isPresent())).as("Organisation '" + code + "' was created").isTrue();
  }
}
