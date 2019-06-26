package com.elvaco.mvp.database.repository.access;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterPk;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterAlarmLogEntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeterAlarmLogsRepository implements MeterAlarmLogs {

  private final MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;
  private final MeasurementJpaRepository measurements;

  @Override
  public AlarmLogEntry save(AlarmLogEntry alarm) {
    MeterAlarmLogEntity entity = MeterAlarmLogEntityMapper.toEntity(alarm);
    return MeterAlarmLogEntityMapper.toDomainModel(meterAlarmLogJpaRepository.save(entity));
  }

  @Override
  public Collection<? extends AlarmLogEntry> save(Collection<? extends AlarmLogEntry> alarms) {
    return alarms.stream()
      .map(MeterAlarmLogEntityMapper::toEntity)
      .map(meterAlarmLogJpaRepository::save)
      .map(MeterAlarmLogEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public void createOrUpdate(PrimaryKey primaryKey, int mask, ZonedDateTime timestamp) {
    meterAlarmLogJpaRepository.createOrUpdate(
      primaryKey.getId(),
      primaryKey.getOrganisationId(),
      mask,
      timestamp
    );
  }

  @Override
  public Stream<AlarmLogEntry> findActiveAlarmsOlderThan(ZonedDateTime when) {
    return meterAlarmLogJpaRepository.findActiveAlarmsOlderThan(when).stream()
      .map(MeterAlarmLogEntityMapper::toDomainModel);
  }

  @Override
  @Transactional
  public void closeAlarmIfNewMeasurementsArrived(AlarmLogEntry alarm, ZonedDateTime toDate) {
    measurements.firstForPhysicalMeter(
      alarm.primaryKey.getOrganisationId(),
      alarm.primaryKey.getId(),
      alarm.lastSeen,
      toDate
    ).ifPresent(firstMeasurementAfterLastSeen ->
      meterAlarmLogJpaRepository.save(MeterAlarmLogEntity.builder()
        .id(alarm.id)
        .pk(new PhysicalMeterPk(alarm.primaryKey.getId(), alarm.primaryKey.getOrganisationId()))
        .mask(alarm.mask)
        .start(alarm.start)
        .lastSeen(alarm.lastSeen)
        .stop(firstMeasurementAfterLastSeen.id.readoutTime)
        .build()));
  }
}
