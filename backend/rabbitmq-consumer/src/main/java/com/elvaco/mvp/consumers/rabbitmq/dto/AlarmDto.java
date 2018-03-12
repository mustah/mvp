package com.elvaco.mvp.consumers.rabbitmq.dto;

public class AlarmDto {
  public final long timestamp;
  public final int code;

  public AlarmDto(long timestamp, int code) {
    this.timestamp = timestamp;
    this.code = code;
  }
}
