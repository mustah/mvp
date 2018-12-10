package com.elvaco.mvp.database.repository.mappers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
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

  @Nullable
  static AlarmLogEntry toLatestActiveAlarm(List<PhysicalMeter> physicalMeters) {
    return physicalMeters.stream()
      .findFirst()
      .flatMap(physicalMeter -> physicalMeter.alarms.stream()
        .filter(AlarmLogEntry::isActive)
        .max(Comparator.comparing(o -> o.start)))
      .orElse(null);
  }
}
