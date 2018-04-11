package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

public interface GatewayStatusLogs {

  void save(StatusLogEntry<UUID> gatewayStatusLog);

  void save(List<StatusLogEntry<UUID>> gatewayStatusLog);
}
