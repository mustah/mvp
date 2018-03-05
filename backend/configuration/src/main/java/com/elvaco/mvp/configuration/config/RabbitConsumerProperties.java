package com.elvaco.mvp.configuration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mvp.consumers.rabbit")
public class RabbitConsumerProperties {

  private String queueName = "MVP";

  public String getQueueName() {
    return queueName;
  }
}
