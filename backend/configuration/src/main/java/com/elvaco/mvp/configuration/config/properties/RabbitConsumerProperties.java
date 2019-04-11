package com.elvaco.mvp.configuration.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mvp.consumers.rabbit")
public class RabbitConsumerProperties {

  private String meteringFanoutExchange = "mvp.fanout";
  private String nbiotTopicExchange = "nbiot-test-ponte";
  private String queueName = "MVP";
  private String responseExchange = "";
  private String responseRoutingKey = "mvp.to.metering";
  private String deadLetterExchange = "mvp.dead.letter";
  private Boolean requeueRejected = false;
  private Integer prefetchCount = 250;
  private Integer txSize = 1;
  private Integer maxPriority = 10;
}
