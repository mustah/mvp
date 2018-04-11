package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

public interface MeterStatusLogs {

  StatusLogEntry<UUID> save(StatusLogEntry<UUID> meterStatusLog);

  void save(List<StatusLogEntry<UUID>> meterStatusLogs);
}
