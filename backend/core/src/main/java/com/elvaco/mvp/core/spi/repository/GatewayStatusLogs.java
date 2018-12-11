package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

public interface GatewayStatusLogs {

  void save(StatusLogEntry gatewayStatusLog);
}
