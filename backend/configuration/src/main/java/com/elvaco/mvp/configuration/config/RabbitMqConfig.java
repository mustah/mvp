package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.consumers.rabbitmq.MeteringMessageReceiver;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageHandler;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(RabbitConsumerProperties.class)
class RabbitMqConfig {

  private final RabbitConsumerProperties consumerProperties;

  @Autowired
  RabbitMqConfig(
    RabbitConsumerProperties consumerProperties
  ) {
    this.consumerProperties = consumerProperties;
  }

  @Bean
  Queue queue() {
    return new Queue(consumerProperties.getQueueName(), false);
  }

  @Bean
  MessageHandler meteringMessageHandler(
    LogicalMeterUseCases logicalMeterUseCases,
    PhysicalMeterUseCases physicalMeterUseCases,
    OrganisationUseCases organisationUseCases,
    MeasurementUseCases measurementUseCases,
    GatewayUseCases gatewayUseCases,
    GeocodeService geocodeService
  ) {
    return new MeteringMessageHandler(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      measurementUseCases,
      gatewayUseCases,
      geocodeService
    );
  }

  @Bean
  MeteringMessageReceiver meteringMessageReceiver(MessageHandler messageHandler) {
    return new MeteringMessageReceiver(messageHandler);
  }

  @Bean
  SimpleMessageListenerContainer container(
    ConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter,
    PlatformTransactionManager transactionManager
  ) {
    SimpleMessageListenerContainer container =
      new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(consumerProperties.getQueueName());
    listenerAdapter.setResponseExchange(consumerProperties.getResponseExchange());
    listenerAdapter.setResponseRoutingKey(consumerProperties.getResponseRoutingKey());
    container.setMessageListener(listenerAdapter);
    container.setChannelTransacted(true);
    container.setTransactionManager(transactionManager);
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(MeteringMessageReceiver meteringMessageReceiver) {
    return new MessageListenerAdapter(new AuthenticatingMeteringMessageReceiver(
      meteringMessageReceiver), "receiveMessage");
  }
}
