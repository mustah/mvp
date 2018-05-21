package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class GatewayEntityMapper {

  static Gateway toDomainModelWithoutStatusLogs(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      emptyList(),
      emptyList()
    );
  }

  public static Gateway toDomainModel(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      emptyList(),
      entity.statusLogs.stream().map(GatewayStatusLogEntityMapper::toDomainModel).collect(toList())
    );
  }

  public static GatewayEntity toEntity(Gateway domainModel) {
    return new GatewayEntity(
      domainModel.id,
      domainModel.organisationId,
      domainModel.serial,
      domainModel.productModel,
      domainModel.statusLogs.stream().map(GatewayStatusLogEntityMapper::toEntity).collect(toSet())
    );
  }
}
