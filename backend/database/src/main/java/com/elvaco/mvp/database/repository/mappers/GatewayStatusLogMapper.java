package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;

public class GatewayStatusLogMapper
  implements DomainEntityMapper<GatewayStatusLog, GatewayStatusLogEntity> {

  @Override
  public GatewayStatusLog toDomainModel(GatewayStatusLogEntity entity) {
    return new GatewayStatusLog(
      entity.id,
      entity.gatewayId,
      entity.status,
      entity.start,
      entity.stop
    );
  }

  @Override
  public GatewayStatusLogEntity toEntity(GatewayStatusLog statusLog) {
    return new GatewayStatusLogEntity(
      statusLog.id,
      statusLog.gatewayId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
