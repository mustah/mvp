package com.elvaco.mvp.configuration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mvp.consumers.rabbit")
public class RabbitConsumerProperties {

  private String queueName = "MVP";
  private String responseExchange = "";
  private String responseRoutingKey = "mvp.to.metering";

  public String getQueueName() {
    return queueName;
  }

  public String getResponseExchange() {
    return responseExchange;
  }

  public String getResponseRoutingKey() {
    return responseRoutingKey;
  }
}
