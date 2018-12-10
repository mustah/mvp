package com.elvaco.mvp.testing.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;

public class MockMeterStatusLogs
  extends MockRepository<Long, StatusLogEntry>
  implements MeterStatusLogs {

  @Override
  public StatusLogEntry save(StatusLogEntry meterStatusLog) {
    return saveMock(meterStatusLog);
  }

  @Override
  public void save(List<StatusLogEntry> meterStatusLogs) {
    meterStatusLogs.forEach(this::saveMock);
  }

  @Override
  protected StatusLogEntry copyWithId(Long id, StatusLogEntry entity) {
    return StatusLogEntry.builder()
      .id(id)
      .primaryKey(entity.primaryKey)
      .status(entity.status)
      .start(entity.start)
      .stop(entity.stop)
      .build();
  }

  @Override
  protected Long generateId(StatusLogEntry entity) {
    return nextId();
  }
}
