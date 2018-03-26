package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

public class MeterStatusLogMapper
  implements DomainEntityMapper<MeterStatusLog, PhysicalMeterStatusLogEntity> {

  @Override
  public MeterStatusLog toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return new MeterStatusLog(
      entity.id,
      entity.physicalMeterId,
      entity.status,
      entity.start,
      entity.stop
    );
  }

  @Override
  public PhysicalMeterStatusLogEntity toEntity(MeterStatusLog statusLog) {
    return new PhysicalMeterStatusLogEntity(
      statusLog.id,
      statusLog.physicalMeterId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
