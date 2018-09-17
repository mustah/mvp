package com.elvaco.mvp.database.repository.access;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterAlarmLogEntityMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterAlarmLogsRepository implements MeterAlarmLogs {

  private final MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;

  @Override
  public void createOrUpdate(
    UUID physicalMeterId,
    int mask,
    ZonedDateTime start,
    ZonedDateTime lastSeen,
    String description
  ) {
    meterAlarmLogJpaRepository.createOrUpdate(
      physicalMeterId,
      mask,
      start,
      lastSeen,
      description
    );
  }

  @Override
  public AlarmLogEntry save(AlarmLogEntry alarm) {
    MeterAlarmLogEntity entity = MeterAlarmLogEntityMapper.toEntity(alarm);
    return MeterAlarmLogEntityMapper.toDomainModel(meterAlarmLogJpaRepository.save(entity));
  }

  @Override
  public void save(Collection<? extends AlarmLogEntry> alarms) {
    alarms.stream()
      .map(MeterAlarmLogEntityMapper::toEntity)
      .forEach(meterAlarmLogJpaRepository::save);
  }
}
