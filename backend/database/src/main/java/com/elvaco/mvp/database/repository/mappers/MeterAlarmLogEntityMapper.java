package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterPk;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class MeterAlarmLogEntityMapper {

  public static AlarmLogEntry toDomainModel(MeterAlarmLogEntity entity) {
    return AlarmLogEntry.builder()
      .id(entity.id)
      .primaryKey(entity.pk)
      .mask(entity.mask)
      .start(entity.start)
      .stop(entity.stop)
      .lastSeen(entity.lastSeen)
      .build();
  }

  public static MeterAlarmLogEntity toEntity(AlarmLogEntry domainModel) {
    PrimaryKey primaryKey = domainModel.primaryKey();
    return MeterAlarmLogEntity.builder()
      .id(domainModel.id)
      .pk(new PhysicalMeterPk(primaryKey.getId(), primaryKey.getOrganisationId()))
      .mask(domainModel.mask)
      .start(domainModel.start)
      .lastSeen(Optional.ofNullable(domainModel.lastSeen).orElse(domainModel.start))
      .stop(domainModel.stop)
      .build();
  }

  @Nullable
  static List<AlarmLogEntry> toLatestActiveAlarms(List<PhysicalMeter> physicalMeters) {
    return physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.alarms.stream()
        .filter(AlarmLogEntry::isActive))
      .collect(toList());
  }
}
