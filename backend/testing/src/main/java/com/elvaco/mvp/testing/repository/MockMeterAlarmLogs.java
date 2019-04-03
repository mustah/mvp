package com.elvaco.mvp.testing.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;

import static java.util.stream.Collectors.toList;

public class MockMeterAlarmLogs
  extends MockRepository<Long, AlarmLogEntry>
  implements MeterAlarmLogs {

  @Override
  public AlarmLogEntry save(AlarmLogEntry alarm) {
    return saveMock(alarm);
  }

  @Override
  public Collection<? extends AlarmLogEntry> save(Collection<? extends AlarmLogEntry> alarms) {
    return alarms.stream().map(this::saveMock).collect(toList());
  }

  @Override
  public void createOrUpdate(
    PrimaryKey primaryKey,
    int mask,
    ZonedDateTime start
  ) {
    saveMock(AlarmLogEntry.builder()
      .primaryKey(primaryKey)
      .mask(mask)
      .start(start)
      .lastSeen(start)
      .build()
    );
  }

  @Override
  public Stream<AlarmLogEntry> findActiveAlarmsOlderThan(ZonedDateTime when) {
    return allMocks().stream().filter(alarm -> alarm.stop == null)
      .filter(alarm -> alarm.start.isBefore(when));
  }

  public Set<AlarmLogEntry> findAll() {
    return new HashSet<>(allMocks());
  }

  @Override
  protected AlarmLogEntry copyWithId(Long id, AlarmLogEntry entity) {
    return AlarmLogEntry.builder()
      .id(id)
      .primaryKey(entity.primaryKey)
      .mask(entity.mask)
      .description(entity.description)
      .start(entity.start)
      .stop(entity.stop)
      .lastSeen(entity.lastSeen)
      .build();
  }

  @Override
  protected Long generateId(AlarmLogEntry entity) {
    return nextId();
  }
}
