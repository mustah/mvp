package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;

public class GatewayMapper implements DomainEntityMapper<Gateway, GatewayEntity> {

  @Override
  public Gateway toDomainModel(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.serial,
      entity.productModel,
      entity.phoneNumber,
      entity.port,
      entity.ip
    );
  }

  @Override
  public GatewayEntity toEntity(Gateway domainModel) {
    return new GatewayEntity(
      domainModel.id,
      domainModel.serial,
      domainModel.productModel,
      domainModel.phoneNumber,
      domainModel.port,
      domainModel.ip
    );
  }
}
