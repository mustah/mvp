package com.elvaco.mvp.consumers.rabbitmq.message;

public class AlarmDto {
  public final long timestamp;
  public final int code;
  public final String description;

  public AlarmDto(long timestamp, int code, String description) {
    this.timestamp = timestamp;
    this.code = code;
    this.description = description;
  }
}
