package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.StatusEntity;

public class GatewayStatusLogMapper implements
                                    DomainEntityMapper<GatewayStatusLog, GatewayStatusLogEntity> {

  @Override
  public GatewayStatusLog toDomainModel(GatewayStatusLogEntity entity) {
    return new GatewayStatusLog(
      entity.id,
      entity.gatewayId,
      entity.status.id,
      entity.status.name,
      entity.start,
      entity.stop
    );
  }

  @Override
  public GatewayStatusLogEntity toEntity(GatewayStatusLog domainModel) {
    GatewayStatusLogEntity entity = new GatewayStatusLogEntity();
    entity.id = domainModel.id;
    entity.gatewayId = domainModel.gatewayId;
    entity.status = new StatusEntity(domainModel.statusId, domainModel.name);
    entity.start = domainModel.start;
    entity.stop = domainModel.stop;

    return entity;
  }
}
