package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class GatewayWithMetersMapper {

  public static Gateway toDomainModel(GatewayEntity entity) {
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

  private static List<LogicalMeter> toLogicalMeters(Set<LogicalMeterEntity> meters) {
    return meters.stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }
}
