package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;

import static java.util.stream.Collectors.toList;

public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository repository;
  private final GatewayMapper mapper;
  private final GatewayWithMetersMapper gatewayWithMetersMapper;

  public GatewayRepository(
    GatewayJpaRepository repository,
    GatewayMapper mapper,
    GatewayWithMetersMapper gatewayWithMetersMapper
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.gatewayWithMetersMapper = gatewayWithMetersMapper;
  }

  @Override
  public List<Gateway> findAll() {
    return repository.findAll()
      .stream()
      .map(gatewayWithMetersMapper::withLogicalMeters)
      .collect(toList());
  }

  @Override
  public List<Gateway> findAllByOrganisationId(UUID organisationId) {
    return repository.findAllByOrganisationId(organisationId)
      .stream()
      .map(gatewayWithMetersMapper::withLogicalMeters)
      .collect(toList());
  }

  @Override
  public Gateway save(Gateway gateway) {
    GatewayEntity entity = repository.save(mapper.toEntity(gateway));
    return gatewayWithMetersMapper.withLogicalMeters(entity);
  }
}
