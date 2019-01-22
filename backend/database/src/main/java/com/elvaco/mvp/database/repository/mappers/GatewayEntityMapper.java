package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class GatewayEntityMapper {

  public static Gateway toDomainModelWithoutStatusLogs(GatewayEntity entity) {
    return Gateway.builder()
      .id(entity.pk.id)
      .organisationId(entity.pk.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .build();
  }

  public static Gateway toDomainModel(GatewayEntity entity) {
    return toDomainModelWithoutStatusLogs(entity).toBuilder()
      .statusLogs(entity.statusLogs.stream()
        .map(GatewayStatusLogEntityMapper::toDomainModel)
        .collect(toList()))
      .build();
  }

  public static GatewayEntity toEntity(Gateway domainModel) {
    return new GatewayEntity(
      new EntityPk(domainModel.id, domainModel.organisationId),
      domainModel.serial,
      domainModel.productModel,
      domainModel.statusLogs.stream().map(GatewayStatusLogEntityMapper::toEntity).collect(toSet())
    );
  }
}
