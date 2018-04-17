package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeterStatusLogMapper {

  public static StatusLogEntry<UUID> toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return new StatusLogEntry<>(
      entity.id,
      entity.physicalMeterId,
      entity.status,
      entity.start,
      entity.stop
    );
  }

  public static PhysicalMeterStatusLogEntity toEntity(StatusLogEntry<UUID> statusLog) {
    return new PhysicalMeterStatusLogEntity(
      statusLog.id,
      statusLog.entityId,
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
