package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import static java.util.stream.Collectors.toList;

public final class GatewayWithMetersMapper {

  private LogicalMeterEntityMapper logicalMeterEntityMapper;

  public GatewayWithMetersMapper(LogicalMeterEntityMapper logicalMeterEntityMapper) {
    this.logicalMeterEntityMapper = logicalMeterEntityMapper;
  }

  public Gateway toDomainModel(GatewayEntity entity) {
    return Gateway.builder()
      .id(entity.pk.id)
      .organisationId(entity.pk.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .meters(toLogicalMeters(entity.meters))
      .statusLogs(entity.statusLogs.stream()
        .map(GatewayStatusLogEntityMapper::toDomainModel)
        .collect(toList()))
      .build();
  }

  private List<LogicalMeter> toLogicalMeters(Set<LogicalMeterEntity> meters) {
    return meters.stream()
      .map(logicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }
}
