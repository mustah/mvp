package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class GatewayWithMetersMapper {

  public static Gateway toDomainModel(
    GatewayEntity entity,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogEntityMap
  ) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      toLogicalMeters(entity.meters),
      statusLogEntityMap.getOrDefault(entity.id, emptyList())
        .stream()
        .map(GatewayStatusLogEntityMapper::toDomainModel)
        .collect(toList())
    );
  }

  public static Gateway toDomainModel(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      toLogicalMeters(entity.meters),
      entity.statusLogs.stream().map(GatewayStatusLogEntityMapper::toDomainModel).collect(toList())
    );
  }

  private static List<LogicalMeter> toLogicalMeters(Set<LogicalMeterEntity> meters) {
    return meters
      .stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }
}
