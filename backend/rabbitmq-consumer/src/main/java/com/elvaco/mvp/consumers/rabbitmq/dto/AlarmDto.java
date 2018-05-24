package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class AlarmDto {
  public final LocalDateTime timestamp;
  public final int code;
}
