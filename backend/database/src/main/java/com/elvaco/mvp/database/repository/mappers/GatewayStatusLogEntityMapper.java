package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.gateway.GatewayPk;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GatewayStatusLogEntityMapper {

  public static StatusLogEntry toDomainModel(GatewayStatusLogEntity entity) {
    return StatusLogEntry.builder()
      .id(entity.id)
      .primaryKey(entity.primaryKey())
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();
  }

  public static GatewayStatusLogEntity toEntity(StatusLogEntry statusLog) {
    return new GatewayStatusLogEntity(
      statusLog.id,
      new GatewayPk(statusLog.primaryKey().getId(), statusLog.primaryKey().getOrganisationId()),
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
