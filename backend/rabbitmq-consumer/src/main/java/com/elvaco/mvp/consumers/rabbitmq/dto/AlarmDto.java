package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;

public class AlarmDto {
  public final LocalDateTime timestamp;
  public final int code;

  public AlarmDto(LocalDateTime timestamp, int code) {
    this.timestamp = timestamp;
    this.code = code;
  }
}
