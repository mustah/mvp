package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;

public interface GatewayStatusLogs {

  void save(GatewayStatusLog gatewayStatusLog);

  void save(List<GatewayStatusLog> gatewayStatusLog);
}
