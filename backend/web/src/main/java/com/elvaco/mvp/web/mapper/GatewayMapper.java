package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.web.dto.GatewayDto;

public class GatewayMapper {

  public GatewayDto toDto(Gateway gateway) {
    return new GatewayDto(
      gateway.id,
      gateway.serial,
      gateway.productModel,
      gateway.phoneNumber,
      gateway.port,
      gateway.ip
    );
  }

  public Gateway toDomainModel(GatewayDto gatewayDto) {
    return new Gateway(
      gatewayDto.id,
      gatewayDto.serial,
      gatewayDto.productModel,
      gatewayDto.phoneNumber,
      gatewayDto.port,
      gatewayDto.ip
    );
  }
}
