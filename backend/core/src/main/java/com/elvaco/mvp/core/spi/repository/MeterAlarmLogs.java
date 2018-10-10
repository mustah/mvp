package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;

public interface MeterAlarmLogs {

  AlarmLogEntry save(AlarmLogEntry alarm);

  void save(Collection<? extends AlarmLogEntry> alarms);

  void createOrUpdate(
    UUID physicalMeterId,
    int mask,
    ZonedDateTime start,
    String description
  );

  Stream<AlarmLogEntry> findActiveAlamsOlderThan(ZonedDateTime when);
}
