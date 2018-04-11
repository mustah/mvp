package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayStatusLogMapper;

import static java.util.stream.Collectors.toList;

public class GatewayStatusLogsRepository implements GatewayStatusLogs {

  private final GatewayStatusLogJpaRepository jpaRepository;
  private final GatewayStatusLogMapper mapper;

  public GatewayStatusLogsRepository(
    GatewayStatusLogJpaRepository jpaRepository,
    GatewayStatusLogMapper mapper
  ) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public void save(StatusLogEntry<UUID> gatewayStatusLog) {
    jpaRepository.save(mapper.toEntity(gatewayStatusLog));
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> gatewayStatusLogs) {
    jpaRepository.save(
      gatewayStatusLogs.stream().map(mapper::toEntity).collect(toList())
    );
  }
}
