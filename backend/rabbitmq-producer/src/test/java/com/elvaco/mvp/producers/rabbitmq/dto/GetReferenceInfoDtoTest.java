package com.elvaco.mvp.producers.rabbitmq.dto;

import com.elvaco.mvp.producers.rabbitmq.MessageSerializer;

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

    assertCanSerialize(dto);
  }

  @Test
  public void serializesCorrectlyWithoutMeter() {
    GetReferenceInfoDto dto = GetReferenceInfoDto.builder()
      .jobId("2")
      .organisationId("organisation")
      .facility(new IdDto("facility-id"))
      .gateway(new IdDto("gateway-id"))
      .build();

    assertCanSerialize(dto);
  }

  @Test
  public void serializesCorrectlyWithoutJobId() {
    GetReferenceInfoDto dto = GetReferenceInfoDto.builder()
      .organisationId("organisation")
      .facility(new IdDto("facility-id"))
      .meter(new IdDto("meter-id"))
      .gateway(new IdDto("gateway-id"))
      .build();

    assertCanSerialize(dto);
  }

  private static void assertCanSerialize(GetReferenceInfoDto dto) {
    String json = MessageSerializer.toJson(dto);
    GetReferenceInfoDto actual = MessageSerializer.fromJson(json, GetReferenceInfoDto.class);

    assertThat(actual.messageType).isEqualTo(MessageType.METERING_GET_REFERENCE_INFO_V_1_0);
    assertThat(actual).isEqualTo(dto);
  }
}
