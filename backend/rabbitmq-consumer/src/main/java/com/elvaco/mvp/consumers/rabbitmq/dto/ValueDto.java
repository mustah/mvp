package com.elvaco.mvp.consumers.rabbitmq.dto;

import java.time.LocalDateTime;

public class ValueDto {
  public final LocalDateTime timestamp;
  public final double value;
  public final String unit;
  public final String quantity;

  public ValueDto(LocalDateTime timestamp, double value, String unit, String quantity) {
    this.timestamp = timestamp;
    this.value = value;
    this.unit = unit;
    this.quantity = quantity;
  }
}
