package com.elvaco.mvp.web.dto;

import com.elvaco.mvp.core.domainmodels.Status;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class GatewayMandatoryDto {

  public String id;
  public String productModel;
  public String serial;
  public Status status;

  public GatewayMandatoryDto(
    String id,
    String productModel,
    String serial,
    Status status
  ) {
    this.id = id;
    this.status = status;
    this.productModel = productModel;
    this.serial = serial;
  }
}
