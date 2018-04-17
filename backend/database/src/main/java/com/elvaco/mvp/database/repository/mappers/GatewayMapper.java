package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import lombok.AllArgsConstructor;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class GatewayMapper implements DomainEntityMapper<Gateway, GatewayEntity> {

  private final GatewayStatusLogMapper gatewayStatusLogMapper;

  Gateway toDomainModelWithoutStatusLogs(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      emptyList(),
      emptyList()
    );
  }

  @Override
  public Gateway toDomainModel(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      emptyList(),
      entity.statusLogs.stream().map(gatewayStatusLogMapper::toDomainModel).collect(toList())
    );
  }

  @Override
  public GatewayEntity toEntity(Gateway domainModel) {
    return new GatewayEntity(
      domainModel.id,
      domainModel.organisationId,
      domainModel.serial,
      domainModel.productModel,
      domainModel.statusLogs.stream().map(gatewayStatusLogMapper::toEntity).collect(toSet())
    );
  }
}
