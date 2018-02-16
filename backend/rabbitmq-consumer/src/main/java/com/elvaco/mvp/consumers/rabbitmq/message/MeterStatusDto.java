package com.elvaco.mvp.consumers.rabbitmq.message;

public class MeterStatusDto {
  public final String id;
  public final String status;

  MeterStatusDto(String id, String status) {
    this.id = id;
    this.status = status;
  }
}
