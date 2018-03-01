package com.elvaco.mvp.consumers.rabbitmq.dto;

public class ValueDto {
  public final long timestamp;
  public final double value;
  public final String unit;
  public final String quantity;

  public ValueDto(long timestamp, double value, String unit, String quantity) {
    this.timestamp = timestamp;
    this.value = value;
    this.unit = unit;
    this.quantity = quantity;
  }
}
