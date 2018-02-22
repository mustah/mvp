package com.elvaco.mvp.web.dto;

public class GatewayDto {

  private Long id;
  private String serial;
  private String productModel;

  public GatewayDto() {}

  public GatewayDto(Long id, String serial, String productModel) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
  }
}
