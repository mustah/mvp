package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.adapters.spring.AmqpMessagePublisher;
import com.elvaco.mvp.consumers.rabbitmq.message.AlarmMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringAlarmMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringReferenceInfoMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.ReferenceInfoMessageConsumer;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.core.util.MessageThrottler;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
  private final PropertiesUseCases propertiesUseCases;
  private final MeterAlarmLogs meterAlarmLogs;

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
  ReferenceInfoMessageConsumer referenceInfoMessageConsumer(
    JobService<MeteringReferenceInfoMessageDto> meterSyncJobService
  ) {
    return new MeteringReferenceInfoMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      gatewayUseCases,
      geocodeService,
      propertiesUseCases,
      meterSyncJobService
    );
  }

  @Bean
  AlarmMessageConsumer alarmMessageConsumer() {
    return new MeteringAlarmMessageConsumer(
      physicalMeterUseCases,
      organisationUseCases,
      meterAlarmLogs
    );
  }

  @Bean
  MessageListener messageListener(
    MeasurementMessageConsumer measurementMessageConsumer,
    ReferenceInfoMessageConsumer referenceInfoMessageConsumer,
    AlarmMessageConsumer alarmMessageConsumer,
    MessageThrottler<String, GetReferenceInfoDto> meteringMessageThrottler
  ) {
    return new MeteringMessageListener(
      new MeteringMessageParser(),
      measurementMessageConsumer,
      referenceInfoMessageConsumer,
      alarmMessageConsumer,
      meteringMessageThrottler
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
    MessagePublisher messagePublisher,
    JobService<MeteringReferenceInfoMessageDto> meterSyncJobService
  ) {
    return new MeteringRequestPublisher(
      currentUser,
      organisations,
      messagePublisher,
      meterSyncJobService
    );
  }

  @Bean
  DirectExchange deadLetterExchange() {
    return (DirectExchange) ExchangeBuilder
      .directExchange(consumerProperties.getDeadLetterExchange())
      .build();
  }

  @Bean
  Queue deadLetterQueue() {
    return QueueBuilder
      .nonDurable("dead-letter-" + consumerProperties.getQueueName())
      .build();
  }

  @Bean
  Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterQueue)
      .to(deadLetterExchange).with(consumerProperties.getQueueName());
  }

  @Bean
  FanoutExchange meteringPublishFanoutExchange() {
    return (FanoutExchange) ExchangeBuilder
      .fanoutExchange(consumerProperties.getMeteringFanoutExchange())
      .durable(true)
      .build();
  }

  @Bean
  Binding meteringBinding(Queue incomingQueue, FanoutExchange meteringPublishFanoutExchange) {
    return BindingBuilder.bind(incomingQueue).to(meteringPublishFanoutExchange);
  }

  @Bean
  AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
    rabbitAdmin.setIgnoreDeclarationExceptions(true);
    return rabbitAdmin;
  }

  @Bean
  Queue incomingQueue() {
    return QueueBuilder
      .durable(consumerProperties.getQueueName())
      .withArgument("x-dead-letter-exchange", consumerProperties.getDeadLetterExchange())
      .withArgument("x-dead-letter-routing-key", consumerProperties.getQueueName())
      .build();
  }

  @Bean
  SimpleMessageListenerContainer container(
    ConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter,
    PlatformTransactionManager transactionManager,
    Queue incomingQueue
  ) {
    SimpleMessageListenerContainer container =
      new SimpleMessageListenerContainer(connectionFactory);

    listenerAdapter.setResponseExchange(consumerProperties.getResponseExchange());
    listenerAdapter.setResponseRoutingKey(consumerProperties.getResponseRoutingKey());
    container.setQueues(incomingQueue);
    container.setDefaultRequeueRejected(consumerProperties.getRequeueRejected());
    container.setMessageListener(listenerAdapter);
    container.setTransactionManager(transactionManager);
    container.setChannelTransacted(true);
    container.setAlwaysRequeueWithTxManagerRollback(consumerProperties.getRequeueRejected());
    container.setPrefetchCount(consumerProperties.getPrefetchCount());
    container.setTxSize(consumerProperties.getTxSize());
    return container;
  }
}
