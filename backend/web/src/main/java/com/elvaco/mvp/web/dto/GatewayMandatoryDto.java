package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class GatewayMandatoryDto {

  public UUID id;
  public String productModel;
  public String serial;
  public String status;
  public String statusChanged;

  public GatewayMandatoryDto(
    UUID id,
    String productModel,
    String serial,
    String status,
    String statusChanged
  ) {
    this.id = id;
    this.status = status;
    this.productModel = productModel;
    this.serial = serial;
    this.statusChanged = statusChanged;
  }
}
