package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GatewayStatusLogEntityMapper {

  public static StatusLogEntry<UUID> toDomainModel(GatewayStatusLogEntity entity) {
    return StatusLogEntry.<UUID>builder()
      .id(entity.id)
      .entityId(entity.gatewayId)
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();

  }

  public static GatewayStatusLogEntity toEntity(StatusLogEntry<UUID> statusLog) {
    return new GatewayStatusLogEntity(
      statusLog.id,
      statusLog.entityId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
