package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;

public interface MeterAlarmLogs {

  AlarmLogEntry save(AlarmLogEntry alarm);

  Collection<AlarmLogEntry> save(Collection<AlarmLogEntry> alarms);

  void createOrUpdate(PrimaryKey primaryKey, int mask, ZonedDateTime start);

  Stream<AlarmLogEntry> findActiveAlarmsOlderThan(ZonedDateTime when);

  void closeAlarmIfNewMeasurementsArrived(AlarmLogEntry alarm, ZonedDateTime toDate);
}
