package com.elvaco.mvp.database.repository.mappers;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeterAlarmLogEntityMapper {

  public static AlarmLogEntry toDomainModel(MeterAlarmLogEntity entity) {
    return AlarmLogEntry.builder()
      .id(entity.id)
      .entityId(entity.physicalMeterId)
      .mask(entity.mask)
      .start(entity.start)
      .stop(entity.stop)
      .lastSeen(entity.lastSeen)
      .description(entity.description)
      .build();
  }

  public static MeterAlarmLogEntity toEntity(AlarmLogEntry domainModel) {
    return MeterAlarmLogEntity.builder()
      .id(domainModel.id)
      .physicalMeterId(domainModel.entityId)
      .mask(domainModel.mask)
      .start(domainModel.start)
      .lastSeen(Optional.ofNullable(domainModel.lastSeen).orElse(domainModel.start))
      .stop(domainModel.stop)
      .description(domainModel.description)
      .build();
  }
}
