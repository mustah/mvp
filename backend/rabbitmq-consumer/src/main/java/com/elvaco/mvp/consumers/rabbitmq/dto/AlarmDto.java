package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlarmDto {
  public final LocalDateTime timestamp;
  public final int code;
}
