package com.elvaco.mvp.testing.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;

public class MockMeterAlarmLogs
  extends MockRepository<Long, AlarmLogEntry>
  implements MeterAlarmLogs {

  @Override
  public void createOrUpdate(
    UUID physicalMeterId,
    int mask,
    ZonedDateTime start,
    String description
  ) {
    saveMock(AlarmLogEntry.builder()
      .entityId(physicalMeterId)
      .mask(mask)
      .start(start)
      .lastSeen(start)
      .description(description)
      .build()
    );
  }

  @Override
  public AlarmLogEntry save(AlarmLogEntry alarm) {
    return saveMock(alarm);
  }

  @Override
  public void save(Collection<? extends AlarmLogEntry> alarms) {
    alarms.forEach(this::saveMock);
  }

  public Set<AlarmLogEntry> findAll() {
    return new HashSet<>(allMocks());
  }

  @Override
  protected AlarmLogEntry copyWithId(Long id, AlarmLogEntry entity) {
    return AlarmLogEntry.builder()
      .id(id)
      .entityId(entity.entityId)
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
