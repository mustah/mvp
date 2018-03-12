package com.elvaco.mvp.consumers.rabbitmq.dto;

public class MeterDto {
  public final String id;
  public final String medium;
  public final String status;
  public final String manufacturer;
  public final int expectedInterval;

  public MeterDto(String id, String medium, String status, String manufacturer, int
    expectedInterval) {
    this.id = id;
    this.medium = medium;
    this.status = status;
    this.manufacturer = manufacturer;
    this.expectedInterval = expectedInterval;
  }
}
