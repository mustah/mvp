package com.elvaco.mvp.database.repository.access;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayStatusLogEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GatewayStatusLogsRepository implements GatewayStatusLogs {

  private final GatewayStatusLogJpaRepository jpaRepository;

  @Override
  public void save(StatusLogEntry gatewayStatusLog) {
    jpaRepository.save(GatewayStatusLogEntityMapper.toEntity(gatewayStatusLog));
  }
}
