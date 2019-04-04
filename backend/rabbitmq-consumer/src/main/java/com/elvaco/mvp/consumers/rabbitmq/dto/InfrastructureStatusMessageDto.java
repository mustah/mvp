package com.elvaco.mvp.consumers.rabbitmq.dto;

import javax.annotation.Nullable;

import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
@Builder(toBuilder = true)
public class InfrastructureStatusMessageDto extends MeteringMessageDto {

  @SerializedName(value = "eui", alternate = "EUI")
  public final String eui;

  @Nullable
  public final JsonNode properties;

  public InfrastructureStatusMessageDto(String eui, JsonNode properties) {
    super(MessageType.INFRASTRUCTURE_STATUS_V_1_0);
    this.eui = eui;
    this.properties = properties;
  }
}
