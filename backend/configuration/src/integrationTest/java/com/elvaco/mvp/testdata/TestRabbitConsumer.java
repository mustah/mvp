package com.elvaco.mvp.testdata;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.elvaco.mvp.producers.rabbitmq.MessageSerializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestRabbitConsumer extends DefaultConsumer {

  private final BlockingQueue<Object> receivedMessages;

  TestRabbitConsumer(
    Channel channel,
    BlockingQueue<Object> receivedMessages
  ) {
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

  public <T> T fromJson(Class<T> classOfT) throws InterruptedException {
    return MessageSerializer.fromJson(new String(receiveOne()), classOfT);
  }

  private byte[] receiveOne() throws InterruptedException {
    return (byte[]) receivedMessages.poll(10, TimeUnit.SECONDS);
  }
}
