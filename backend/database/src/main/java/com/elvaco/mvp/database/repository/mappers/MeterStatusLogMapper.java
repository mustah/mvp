package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.StatusEntity;

public class MeterStatusLogMapper implements
                                  DomainEntityMapper<MeterStatusLog, PhysicalMeterStatusLogEntity> {

  @Override
  public MeterStatusLog toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return new MeterStatusLog(
      entity.id,
      entity.physicalMeterId,
      entity.status.id,
      entity.status.name,
      entity.start,
      entity.stop
    );
  }

  @Override
  public PhysicalMeterStatusLogEntity toEntity(MeterStatusLog domainModel) {
    PhysicalMeterStatusLogEntity entity = new PhysicalMeterStatusLogEntity();
    entity.id = domainModel.id;
    entity.physicalMeterId = domainModel.physicalMeterId;
    entity.status = new StatusEntity(domainModel.statusId, domainModel.name);
    entity.start = domainModel.start;
    entity.stop = domainModel.stop;

    return entity;
  }
}
