package com.elvaco.mvp.testdata;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import com.elvaco.mvp.configuration.config.properties.RabbitConsumerProperties;
import com.rabbitmq.client.Channel;
import org.junit.After;
import org.junit.Before;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.emptyMap;

public abstract class RabbitIntegrationTest extends IntegrationTest {

  @Autowired
  private RabbitConsumerProperties consumerProperties;

  @Autowired
  private ConnectionFactory connectionFactory;

  private Connection connection;
  private Channel channel;

  private boolean rabbitConnected;
  private String consumerTag;

  @Before
  public void setUp_Rabbit() {
    try {
      connection = connectionFactory.createConnection();
      channel = connection.createChannel(false);
      rabbitConnected = true;
    } catch (AmqpConnectException ex) {
      if (connection != null) {
        connection.close();
      }
      rabbitConnected = false;
    }
  }

  @After
  public void tearDown_Rabbit() throws IOException, TimeoutException {
    if (consumerTag != null) {
      channel.basicCancel(consumerTag);
    }
    if (channel != null) {
      channel.close();
    }
    if (connection != null) {
      connection.close();
    }
    rabbitConnected = false;
  }

  protected boolean isRabbitConnected() {
    return rabbitConnected;
  }

  protected TestRabbitConsumer newResponseConsumer() throws IOException {
    TestRabbitConsumer consumer = new TestRabbitConsumer(channel, new LinkedBlockingQueue<>());
    channel.queueDeclare(
      consumerProperties.getResponseRoutingKey(),
      false,
      true,
      true,
      emptyMap()
    );
    consumerTag = channel.basicConsume(consumerProperties.getResponseRoutingKey(), consumer);
    return consumer;
  }

  protected void publishMessage(byte[] message) throws IOException {
    channel.basicPublish("", consumerProperties.getQueueName(), null, message);
  }
}
