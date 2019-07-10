package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public final class GatewayWithMetersMapper {

  private final LogicalMeterEntityMapper logicalMeterEntityMapper;

  public Gateway toDomainModel(GatewayEntity entity) {
    return Gateway.builder()
      .id(entity.pk.id)
      .organisationId(entity.pk.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .ip(entity.ip)
      .phoneNumber(entity.phoneNumber)
      .meters(toLogicalMeters(entity.gatewayMeters.stream()
        .map(gm -> gm.logicalMeter)
        .collect(toSet())))
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
