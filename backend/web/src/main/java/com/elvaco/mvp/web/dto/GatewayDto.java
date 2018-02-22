package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class GatewayDto {

  public Long id;
  public String serial;
  public String productModel;
  @Nullable
  public String phoneNumber;
  @Nullable
  public String port;
  @Nullable
  public String ip;

  public GatewayDto() {}

  public GatewayDto(
    Long id,
    String serial,
    String productModel,
    @Nullable String phoneNumber,
    @Nullable String port,
    @Nullable String ip
  ) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
    this.phoneNumber = phoneNumber;
    this.port = port;
    this.ip = ip;
  }

  public GatewayDto(Long id, String serial, String productModel) {
    this(id, serial, productModel, null, null, null);
  }
}
