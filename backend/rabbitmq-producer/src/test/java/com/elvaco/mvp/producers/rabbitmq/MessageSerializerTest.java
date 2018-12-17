package com.elvaco.mvp.producers.rabbitmq;

import java.time.LocalDateTime;

import org.junit.Test;

import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.fromJson;
import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.toJson;
import static org.assertj.core.api.Assertions.assertThat;

public class MessageSerializerTest {

  @Test
  public void toJson_IncludingLocalDateTime() {
    String asText = "2018-08-26T08:45:49";
    var localDateTime = LocalDateTime.parse(asText);
    var json = toJson(localDateTime);
    assertThat(json).isEqualTo(String.format("\"%s\"", asText));
  }

  @Test
  public void fromJson_IncludingLocalDateTime() {
    String asText = "2016-11-09T11:44:44";
    var localDateTime = fromJson(String.format("\"%s\"", asText), LocalDateTime.class);
    assertThat(localDateTime).isEqualTo(LocalDateTime.parse(asText));
  }
}
