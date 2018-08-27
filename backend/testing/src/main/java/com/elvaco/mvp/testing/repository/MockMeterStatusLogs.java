package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;

public class MockMeterStatusLogs
  extends MockRepository<Long, StatusLogEntry<UUID>>
  implements MeterStatusLogs {

  @Override
  public StatusLogEntry<UUID> save(StatusLogEntry<UUID> meterStatusLog) {
    return saveMock(meterStatusLog);
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> meterStatusLogs) {
    meterStatusLogs.forEach(this::saveMock);
  }

  @Override
  protected StatusLogEntry<UUID> copyWithId(Long id, StatusLogEntry<UUID> entity) {
    return StatusLogEntry.<UUID>builder()
      .id(id)
      .entityId(entity.entityId)
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();
  }

  @Override
  protected Long generateId(StatusLogEntry<UUID> entity) {
    return nextId();
  }
}
