package com.elvaco.mvp.consumers.rabbitmq.dto;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class GatewayStatusDto {
  public final String id;
  public final String productModel;
  public final String status;
}
