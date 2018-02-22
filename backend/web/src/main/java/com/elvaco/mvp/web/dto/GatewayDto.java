package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class GatewayDto {

  public Long id;
  public String serial;
  public String productModel;

  public GatewayDto() {}

  public GatewayDto(Long id, String serial, String productModel) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
  }
}
