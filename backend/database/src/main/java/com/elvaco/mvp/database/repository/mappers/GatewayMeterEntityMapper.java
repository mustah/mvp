package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.database.entity.gateway.GatewayMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

public @UtilityClass class GatewayMeterEntityMapper {

  public static Gateway toDomainModel(GatewayMeterEntity entity) {
    return GatewayEntityMapper.toDomainModel(entity.gateway).toBuilder()
      .created(entity.created)
      .lastSeen(entity.lastSeen)
      .build();
  }

  public static GatewayMeterEntity toEntity(Gateway gateway, LogicalMeterEntity logicalMeter) {
    return new GatewayMeterEntity(
      GatewayEntityMapper.toEntity(gateway),
      logicalMeter,
      gateway.created,
      gateway.lastSeen
    );
  }
}
