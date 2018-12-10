package com.elvaco.mvp.testdata;

import java.io.IOException;
import java.nio.charset.Charset;
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
  ) throws IOException {
    receivedMessages.add(body);
    getChannel().basicAck(envelope.getDeliveryTag(), false);
  }

  public <T> T fromJson(Class<T> classOfT) throws InterruptedException {
    return MessageSerializer.fromJson(new String(receiveOne(), Charset.forName("UTF-8")), classOfT);
  }

  private byte[] receiveOne() throws InterruptedException {
    return (byte[]) receivedMessages.poll(10, TimeUnit.SECONDS);
  }
}
