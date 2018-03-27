package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.queryfilters.QueryFilters;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static java.util.stream.Collectors.toList;

public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository repository;
  private final QueryFilters gatewayQueryFilters;
  private final GatewayMapper mapper;
  private final GatewayWithMetersMapper gatewayWithMetersMapper;
  private final GatewayStatusLogJpaRepository statusLogJpaRepository;

  public GatewayRepository(
    GatewayJpaRepository repository,
    QueryFilters gatewayQueryFilters,
    GatewayMapper mapper,
    GatewayWithMetersMapper gatewayWithMetersMapper,
    GatewayStatusLogJpaRepository statusLogJpaRepository
  ) {
    this.repository = repository;
    this.gatewayQueryFilters = gatewayQueryFilters;
    this.mapper = mapper;
    this.gatewayWithMetersMapper = gatewayWithMetersMapper;
    this.statusLogJpaRepository = statusLogJpaRepository;
  }

  @Override
  public List<Gateway> findAll(RequestParameters parameters) {
    return repository.findAll(toPredicate(parameters))
      .stream()
      .map(gatewayWithMetersMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<Gateway> findAll(RequestParameters parameters, Pageable pageable) {
    org.springframework.data.domain.Page<GatewayEntity> all = repository.findAll(
      toPredicate(parameters),
      new PageRequest(
        pageable.getPageNumber(),
        pageable.getPageSize()
      )
    );

    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap = statusLogJpaRepository
      .findAllGroupedByGatewayId(toPredicate(all.getContent()));

    return new PageAdapter<>(
      all.map((entity) -> gatewayWithMetersMapper.toDomainModel(entity, statusLogMap))
    );
  }

  @Transactional
  @Override
  public List<Gateway> findAllByOrganisationId(UUID organisationId) {
    List<GatewayEntity> gateways = repository.findAllByOrganisationId(organisationId);

    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap = statusLogJpaRepository
      .findAllGroupedByGatewayId(toPredicate(gateways));

    return toGateways(gateways, statusLogMap);
  }

  @Override
  public Gateway save(Gateway gateway) {
    GatewayEntity entity = repository.save(mapper.toEntity(gateway));
    return gatewayWithMetersMapper.toDomainModel(entity);
  }

  @Override
  public Optional<Gateway> findBy(UUID organisationId, String productModel, String serial) {
    return repository.findByOrganisationIdAndProductModelAndSerial(
      organisationId,
      productModel,
      serial
    ).map(mapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findById(UUID id) {
    return repository.findById(id).map(gatewayWithMetersMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return repository.findByOrganisationIdAndId(organisationId, id)
      .map(gatewayWithMetersMapper::toDomainModel);
  }

  private List<Gateway> toGateways(
    List<GatewayEntity> gatewayEntities,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap
  ) {
    return gatewayEntities
      .stream()
      .map(gateway -> gatewayWithMetersMapper.toDomainModel(gateway, statusLogMap))
      .collect(toList());
  }

  private Predicate toPredicate(List<GatewayEntity> content) {
    return gatewayStatusLogEntity.gatewayId.in(getGatewayIds(content));
  }

  private Predicate toPredicate(RequestParameters parameters) {
    return gatewayQueryFilters.toExpression(parameters);
  }

  private List<UUID> getGatewayIds(List<GatewayEntity> gatewayEntities) {
    return gatewayEntities.stream()
      .map(gatewayEntity -> gatewayEntity.id)
      .collect(toList());
  }
}
