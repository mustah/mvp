package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.database.entity.meter.MeterStatusEntity;

public class MeterStatusMapper implements
  DomainEntityMapper<MeterStatus, MeterStatusEntity> {

  @Override
  public MeterStatus toDomainModel(MeterStatusEntity entity) {
    return new MeterStatus(entity.id, entity.name);
  }

  @Override
  public MeterStatusEntity toEntity(MeterStatus domainModel) {
    return new MeterStatusEntity(domainModel.id, domainModel.name);
  }
}
