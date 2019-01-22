package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

public interface GatewayStatusLogs {

  void save(StatusLogEntry gatewayStatusLog);

  void save(List<StatusLogEntry> statusLogEntries);
}
