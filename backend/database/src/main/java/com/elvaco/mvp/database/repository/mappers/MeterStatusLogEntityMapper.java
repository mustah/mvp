package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeterStatusLogEntityMapper {

  public static StatusLogEntry<UUID> toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return StatusLogEntry.<UUID>builder()
      .id(entity.id)
      .entityId(entity.physicalMeterId)
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();
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
