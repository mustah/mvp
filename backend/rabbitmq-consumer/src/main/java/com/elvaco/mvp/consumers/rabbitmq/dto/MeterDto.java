package com.elvaco.mvp.consumers.rabbitmq.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterDto {

  public final String id;
  public final String medium;
  public final String status;
  public final String manufacturer;
  public final int expectedInterval;
}
