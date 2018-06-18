package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.gateway.PagedGateway;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class GatewayWithMetersMapper {

  public static Gateway toDomainModel(
    GatewayEntity entity,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap
  ) {
    return Gateway.builder()
      .id(entity.id)
      .organisationId(entity.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .meters(toLogicalMeters(entity.meters))
      .statusLogs(toStatusLogs(entity.id, statusLogMap))
      .build();
  }

  public static Gateway toDomainModel(GatewayEntity entity) {
    return Gateway.builder()
      .id(entity.id)
      .organisationId(entity.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .meters(toLogicalMeters(entity.meters))
      .statusLogs(entity.statusLogs.stream()
        .map(GatewayStatusLogEntityMapper::toDomainModel)
        .collect(toList()))
      .build();
  }

  public static Gateway ofPageableDomainModel(
    PagedGateway pagedGateway,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap
  ) {
    return Gateway.builder()
      .id(pagedGateway.id)
      .organisationId(pagedGateway.organisationId)
      .serial(pagedGateway.serial)
      .productModel(pagedGateway.productModel)
      .meters(toLogicalMeters(pagedGateway.meters))
      .statusLogs(toStatusLogs(pagedGateway.id, statusLogMap))
      .build();
  }

  private static List<StatusLogEntry<UUID>> toStatusLogs(
    UUID id,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap
  ) {
    return statusLogMap.getOrDefault(id, emptyList()).stream()
      .map(GatewayStatusLogEntityMapper::toDomainModel)
      .collect(toList());
  }

  private static List<LogicalMeter> toLogicalMeters(Set<LogicalMeterEntity> meters) {
    return meters.stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }
}
