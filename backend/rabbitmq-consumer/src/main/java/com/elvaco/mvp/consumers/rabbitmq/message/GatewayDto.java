package com.elvaco.mvp.consumers.rabbitmq.message;

public class GatewayDto {
  public final String id;
  public final String productModel;

  public GatewayDto(String id, String productModel) {
    this.id = id;
    this.productModel = productModel;
  }
}
