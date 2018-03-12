package com.elvaco.mvp.consumers.rabbitmq.dto;

public class GatewayStatusDto {
  public final String id;
  public final String productModel;
  public final String status;

  public GatewayStatusDto(String id, String productModel, String status) {
    this.id = id;
    this.productModel = productModel;
    this.status = status;
  }
}
