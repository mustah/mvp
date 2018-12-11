package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

public interface MeterStatusLogs {

  StatusLogEntry save(StatusLogEntry meterStatusLog);

  void save(List<StatusLogEntry> meterStatusLogs);
}
