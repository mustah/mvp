package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayMandatoryDto {

  public UUID id;
  public String productModel;
  public String serial;
  public String status;
  public String statusChanged;
  public String ip;
  public String phoneNumber;
}
