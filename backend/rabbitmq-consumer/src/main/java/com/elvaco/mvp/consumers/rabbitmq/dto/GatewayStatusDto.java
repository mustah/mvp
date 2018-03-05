package com.elvaco.mvp.consumers.rabbitmq.dto;

public class GatewayStatusDto {
  public final String id;
  public final String status;

  public GatewayStatusDto(String id, String status) {
    this.id = id;
    this.status = status;
  }
}