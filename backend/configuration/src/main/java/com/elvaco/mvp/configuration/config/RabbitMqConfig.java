package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.adapters.spring.AmqpMessagePublisher;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringReferenceInfoMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.ReferenceInfoMessageConsumer;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
  ReferenceInfoMessageConsumer referenceInfoMessageConsumer() {
    return new MeteringReferenceInfoMessageConsumer(
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
    ReferenceInfoMessageConsumer referenceInfoMessageConsumer
  ) {
    return new MeteringMessageListener(
      new MeteringMessageParser(),
      measurementMessageConsumer,
      referenceInfoMessageConsumer
    );
  }

  @Bean
  MessageListenerAdapter listenerAdapter(MessageListener messageListener) {
    return new MessageListenerAdapter(new AuthenticatedMessageListener(messageListener));
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setExchange(consumerProperties.getResponseExchange());
    rabbitTemplate.setRoutingKey(consumerProperties.getResponseRoutingKey());
    return rabbitTemplate;
  }

  @Bean
  MessagePublisher messagePublisher(RabbitTemplate rabbitTemplate) {
    return new AmqpMessagePublisher(rabbitTemplate);
  }

  @Bean
  MeteringRequestPublisher meteringRequestPublisher(
    AuthenticatedUser currentUser,
    Organisations organisations,
    MessagePublisher messagePublisher
  ) {
    return new MeteringRequestPublisher(
      currentUser,
      organisations,
      messagePublisher
    );
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
