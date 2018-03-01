package com.elvaco.mvp.consumers.rabbitmq.dto;

public class MeterStatusDto {
  public final String id;
  public final String status;

  public MeterStatusDto(String id, String status) {
    this.id = id;
    this.status = status;
  }
}
