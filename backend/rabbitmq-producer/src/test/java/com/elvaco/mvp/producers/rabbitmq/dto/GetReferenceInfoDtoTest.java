package com.elvaco.mvp.producers.rabbitmq.dto;

import com.elvaco.mvp.producers.rabbitmq.MessageSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetReferenceInfoDtoTest {

  @Test
  public void serializesCorrectly() {
    GetReferenceInfoDto dto = GetReferenceInfoDto.builder()
      .jobId("1")
      .organisationId("organisation")
      .facility(new IdDto("facility-id"))
      .meter(new IdDto("meter-id"))
      .gateway(new IdDto("gateway-id"))
      .build();

    assertJsonEqual(
      MessageSerializer.toJson(dto),
      "{\"message_type\":\"Elvaco MVP MQ Get Reference Info Message 1.0\","
      + "\"organisation_id\":\"organisation\",\"source_system_id\":\"Elvaco Evo\","
      + "\"jobId\":\"1\",\"facility\":{\"id\":\"facility-id\"},\"meter\":{\"id\":\"meter-id\"},"
      + "\"gateway\":{\"id\":\"gateway-id\"}}"
    );
  }

  @Test
  public void serializesCorrectlyWithoutMeter() {
    GetReferenceInfoDto dto = GetReferenceInfoDto.builder()
      .organisationId("organisation")
      .jobId("1")
      .facility(new IdDto("facility-id"))
      .gateway(new IdDto("gateway-id"))
      .build();
    assertJsonEqual(
      MessageSerializer.toJson(dto),
      "{\"message_type\":\"Elvaco MVP MQ Get Reference Info Message 1.0\","
      + "\"organisation_id\":\"organisation\",\"source_system_id\":\"Elvaco Evo\","
      + "\"facility\":{\"id\":\"facility-id\"},\"jobId\":\"1\","
      + "\"gateway\":{\"id\":\"gateway-id\"}}"
    );
  }

  @Test
  public void serializesCorrectlyWithoutJobId() {
    GetReferenceInfoDto dto = GetReferenceInfoDto.builder()
      .organisationId("organisation")
      .facility(new IdDto("facility-id"))
      .meter(new IdDto("meter-id"))
      .gateway(new IdDto("gateway-id"))
      .build();
    assertJsonEqual(
      MessageSerializer.toJson(dto),
      "{\"message_type\":\"Elvaco MVP MQ Get Reference Info Message 1.0\","
      + "\"organisation_id\":\"organisation\",\"source_system_id\":\"Elvaco Evo\","
      + "\"facility\":{\"id\":\"facility-id\"},\"meter\":{\"id\":\"meter-id\"},"
      + "\"gateway\":{\"id\":\"gateway-id\"}}"
    );
  }

  private void assertJsonEqual(String actualJson, String expectedJson) {
    JsonParser parser = new JsonParser();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonElement actual = parser.parse(actualJson);
    JsonElement expected = parser.parse(expectedJson);
    assertThat(actual)
      .as("\n%s\nis not equal to\n%s\n", gson.toJson(actual), gson.toJson(expected))
      .isEqualTo(expected);
  }

}
