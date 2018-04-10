package com.elvaco.mvp.configuration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mvp.consumers.rabbit")
@Getter
@Setter
public class RabbitConsumerProperties {

  private String queueName = "MVP";
  private String responseExchange = "";
  private String responseRoutingKey = "mvp.to.metering";
  private Boolean requeueRejected = false;

}
