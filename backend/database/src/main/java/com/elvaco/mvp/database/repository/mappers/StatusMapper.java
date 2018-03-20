package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.database.entity.meter.StatusEntity;

public class StatusMapper implements
  DomainEntityMapper<Status, StatusEntity> {

  @Override
  public Status toDomainModel(StatusEntity entity) {
    return new Status(entity.id, entity.name);
  }

  @Override
  public StatusEntity toEntity(Status domainModel) {
    return new StatusEntity(domainModel.id, domainModel.name);
  }
}
