package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GatewayStatusLogMapper {

  public static StatusLogEntry<UUID> toDomainModel(GatewayStatusLogEntity entity) {
    return new StatusLogEntry<>(
      entity.id,
      entity.gatewayId,
      entity.status,
      entity.start,
      entity.stop
    );
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
