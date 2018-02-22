package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayMapper;

import static java.util.stream.Collectors.toList;

public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository repository;
  private final GatewayMapper gatewayMapper;

  public GatewayRepository(GatewayJpaRepository repository, GatewayMapper gatewayMapper) {
    this.repository = repository;
    this.gatewayMapper = gatewayMapper;
  }

  @Override
  public List<Gateway> findAll() {
    return repository.findAll()
      .stream()
      .map(gatewayMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Gateway save(Gateway gateway) {
    GatewayEntity entity = repository.save(gatewayMapper.toEntity(gateway));
    return gatewayMapper.toDomainModel(entity);
  }
}
