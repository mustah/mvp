package com.elvaco.mvp.adapters.spring;

import com.elvaco.mvp.core.spi.amqp.MessagePublisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageBuilder;

public class AmqpMessagePublisher implements MessagePublisher {

  private final AmqpTemplate amqpTemplate;

  public AmqpMessagePublisher(AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  @Override
  public void publish(byte[] messageBody) {
    amqpTemplate.send(MessageBuilder.withBody(messageBody).build());
  }
}
