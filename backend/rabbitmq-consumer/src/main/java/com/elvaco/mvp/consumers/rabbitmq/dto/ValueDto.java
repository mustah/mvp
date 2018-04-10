package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class ValueDto {

  public final LocalDateTime timestamp;
  public final double value;
  public final String unit;
  public final String quantity;

  public ValueDto withUnit(String unit) {
    return new ValueDto(
      timestamp,
      value,
      unit,
      quantity
    );
  }
}
