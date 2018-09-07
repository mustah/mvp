package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class AlarmDto {

  public final LocalDateTime timestamp;
  public final int mask;
  @Nullable
  public final String description;
}
