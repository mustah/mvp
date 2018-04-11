package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

public class MeterStatusLogMapper
  implements DomainEntityMapper<StatusLogEntry<UUID>, PhysicalMeterStatusLogEntity> {

  @Override
  public StatusLogEntry<UUID> toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return new StatusLogEntry<>(
      entity.id,
      entity.physicalMeterId,
      entity.status,
      entity.start,
      entity.stop
    );
  }

  @Override
  public PhysicalMeterStatusLogEntity toEntity(StatusLogEntry<UUID> statusLog) {
    return new PhysicalMeterStatusLogEntity(
      statusLog.id,
      statusLog.entityId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
