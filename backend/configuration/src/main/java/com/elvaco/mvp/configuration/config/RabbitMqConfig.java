package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringStructureMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.StructureMessageConsumer;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(RabbitConsumerProperties.class)
class RabbitMqConfig {

  private final RabbitConsumerProperties consumerProperties;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final GeocodeService geocodeService;

  @Bean
  Queue queue() {
    return new Queue(consumerProperties.getQueueName(), false);
  }

  @Bean
  MeasurementMessageConsumer measurementMessageConsumer() {
    return new MeteringMeasurementMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      measurementUseCases,
      gatewayUseCases
    );
  }

  @Bean
  StructureMessageConsumer structureMessageConsumer() {
    return new MeteringStructureMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      gatewayUseCases,
      geocodeService
    );
  }

  @Bean
  MessageListener messageListener(
    MeasurementMessageConsumer measurementMessageConsumer,
    StructureMessageConsumer structureMessageConsumer
  ) {
    return new MeteringMessageListener(
      new MeteringMessageParser(),
      measurementMessageConsumer,
      structureMessageConsumer
    );
  }

  @Bean
  MessageListenerAdapter listenerAdapter(MessageListener messageListener) {
    return new MessageListenerAdapter(new AuthenticatedMessageListener(messageListener));
  }

  @Bean
  SimpleMessageListenerContainer container(
    ConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter,
    PlatformTransactionManager transactionManager
  ) {
    SimpleMessageListenerContainer container =
      new SimpleMessageListenerContainer(connectionFactory);
    listenerAdapter.setResponseExchange(consumerProperties.getResponseExchange());
    listenerAdapter.setResponseRoutingKey(consumerProperties.getResponseRoutingKey());
    container.setQueueNames(consumerProperties.getQueueName());
    container.setDefaultRequeueRejected(consumerProperties.getRequeueRejected());
    container.setMessageListener(listenerAdapter);
    container.setTransactionManager(transactionManager);
    container.setChannelTransacted(true);
    container.setAlwaysRequeueWithTxManagerRollback(consumerProperties.getRequeueRejected());
    return container;
  }
}
