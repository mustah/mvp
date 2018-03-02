package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.consumers.rabbitmq.MeteringMessageReceiver;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageHandler;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitMqConfig {

  private static final String QUEUE_NAME = "MVP";

  @Bean
  Queue queue() {
    return new Queue(QUEUE_NAME, false);
  }

  @Bean
  MessageHandler meteringMessageHandler(
    LogicalMeters logicalMeters,
    PhysicalMeters physicalMeters,
    Organisations organisations,
    MeasurementUseCases measurementUseCases
  ) {
    return new MeteringMessageHandler(
      logicalMeters,
      physicalMeters,
      organisations,
      measurementUseCases
    );
  }

  @Bean
  MeteringMessageReceiver meteringMessageReceiver(MessageHandler messageHandler) {
    return new MeteringMessageReceiver(messageHandler);
  }

  @Bean
  SimpleMessageListenerContainer container(
    ConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter
  ) {
    SimpleMessageListenerContainer container =
      new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setMessageListener(listenerAdapter);
    container.setDefaultRequeueRejected(false);
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(MeteringMessageReceiver meteringMessageReceiver) {
    return new MessageListenerAdapter(meteringMessageReceiver, "receiveMessage");
  }
}
