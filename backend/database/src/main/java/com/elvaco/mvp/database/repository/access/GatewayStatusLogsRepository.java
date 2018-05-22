package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayStatusLogEntityMapper;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GatewayStatusLogsRepository implements GatewayStatusLogs {

  private final GatewayStatusLogJpaRepository jpaRepository;

  @Override
  public void save(StatusLogEntry<UUID> gatewayStatusLog) {
    jpaRepository.save(GatewayStatusLogEntityMapper.toEntity(gatewayStatusLog));
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> gatewayStatusLogs) {
    jpaRepository.save(
      gatewayStatusLogs.stream().map(GatewayStatusLogEntityMapper::toEntity).collect(toList())
    );
  }
}
