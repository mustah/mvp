package com.elvaco.mvp.consumers.rabbitmq.message;

public class GatewayStatusDto {
  public final String id;
  public final String status;

  GatewayStatusDto(String id, String status) {
    this.id = id;
    this.status = status;
  }
}
