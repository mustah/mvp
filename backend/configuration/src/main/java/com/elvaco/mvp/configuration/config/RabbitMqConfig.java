package com.elvaco.mvp.configuration.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.AmqpMessagePublisher;
import com.elvaco.mvp.configuration.config.properties.RabbitConsumerProperties;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.producers.rabbitmq.MeteringRequestPublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.web.security.MvpUserDetails;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Scope;
import co.elastic.apm.api.Transaction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(RabbitConsumerProperties.class)
class RabbitMqConfig {

  private final RabbitConsumerProperties consumerProperties;

  @Bean
  MessageListenerAdapter listenerAdapter(
    MessageListener messageListener,
    Organisation rootOrganisation
  ) {
    return new MessageListenerAdapter(new AuthenticatedMessageListener(
      messageListener,
      rootOrganisation
    ));
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
      .durable("dead-letter-" + consumerProperties.getQueueName())
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
      .withArgument("x-max-priority", consumerProperties.getMaxPriority())
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
    container.setDefaultRequeueRejected(true);
    container.setMessageListener(listenerAdapter);
    container.setTransactionManager(transactionManager);
    container.setChannelTransacted(true);
    container.setAlwaysRequeueWithTxManagerRollback(consumerProperties.getRequeueRejected());
    container.setPrefetchCount(consumerProperties.getPrefetchCount());
    container.setTxSize(consumerProperties.getTxSize());
    container.setConcurrency("1-4");
    container.setAdviceChain(
      RetryInterceptorBuilder
        .stateful()
        .messageKeyGenerator(message -> Arrays.toString(message.getBody()))
        .maxAttempts(4)
        .backOffOptions(1000, 1.5, 3000)
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build()
    );
    return container;
  }

  @Slf4j
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static class AuthenticatedMessageListener {

    private final MessageListener messageListener;
    private final Organisation rootOrganisation;

    /**
     * This is used through reflection in com.elvaco.mvp.configuration.config.RabbitMqConfig.
     *
     * @param message Message received from queue.
     *
     * @return A serialized json string containing response information to be placed on the response
     *   queue. When this is {@code null} no message will be places on the response routing queue.
     */
    @Nullable
    @SuppressWarnings("unused")
    public String handleMessage(byte[] message) {
      Transaction transaction = ElasticApm.startTransaction();
      try (Scope scope = transaction.activate()) {
        transaction.setType("rabbitmq");
        transaction.setName("AuthenticatedMessageListener#handleMessage");
        SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(new MvpUserDetails(meteringUser(), ""), null)
        );
        String encodedMessage = toEncodedMessage(message);

        AuthenticatedMessageListener.log.debug(
          "Received message from RabbitMQ: {}",
          encodedMessage
        );

        return messageListener.onMessage(encodedMessage);
      } catch (Exception ex) {
        transaction.captureException(ex);
        throw ex;
      } finally {
        SecurityContextHolder.clearContext();
        transaction.end();
      }
    }

    private User meteringUser() {
      return new User(
        randomUUID(),
        "Metering Message RabbitMQ Consumer",
        "noone@example.com",
        "",
        Language.sv,
        rootOrganisation,
        List.of(Role.SUPER_ADMIN)
      );
    }

    private static String toEncodedMessage(byte[] message) {
      return new String(message, StandardCharsets.UTF_8);
    }
  }
}
