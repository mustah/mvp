package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;

public class GatewayStatusLogMapper
  implements DomainEntityMapper<StatusLogEntry<UUID>, GatewayStatusLogEntity> {

  @Override
  public StatusLogEntry<UUID> toDomainModel(GatewayStatusLogEntity entity) {
    return new StatusLogEntry<>(
      entity.id,
      entity.gatewayId,
      entity.status,
      entity.start,
      entity.stop
    );
  }

  @Override
  public GatewayStatusLogEntity toEntity(StatusLogEntry<UUID> statusLog) {
    return new GatewayStatusLogEntity(
      statusLog.id,
      statusLog.entityId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
