package com.elvaco.mvp.producers.rabbitmq.dto;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class GatewayStatusDto {
  public final String id;
  public final String productModel;
  public final String status;
  public final String ip;
  public final String phoneNumber;
}
