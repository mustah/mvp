package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterPk;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeterStatusLogEntityMapper {

  public static StatusLogEntry toDomainModel(PhysicalMeterStatusLogEntity entity) {
    return StatusLogEntry.builder()
      .id(entity.id)
      .primaryKey(entity.pk)
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();
  }

  public static PhysicalMeterStatusLogEntity toEntity(StatusLogEntry statusLog) {
    return new PhysicalMeterStatusLogEntity(
      statusLog.id,
      new PhysicalMeterPk(
        statusLog.primaryKey().getId(),
        statusLog.primaryKey().getOrganisationId()
      ),
      statusLog.status,
      statusLog.start,
      statusLog.stop
    );
  }
}
