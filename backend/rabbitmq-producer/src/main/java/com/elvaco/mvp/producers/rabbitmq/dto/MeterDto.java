package com.elvaco.mvp.producers.rabbitmq.dto;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class MeterDto {

  public final String id;
  public final String medium;
  public final String status;
  public final String manufacturer;
  @Nullable
  public final String cron;
  @Nullable
  public final Integer revision;
  @Nullable
  public final Integer mbusDeviceType;
}
