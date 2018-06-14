package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayEntityMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.queryfilters.QueryFilters;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository repository;
  private final QueryFilters gatewayQueryFilters;
  private final QueryFilters gatewayStatusLogQueryFilters;
  private final GatewayStatusLogJpaRepository statusLogJpaRepository;

  @Override
  public List<Gateway> findAll(RequestParameters parameters) {
    return repository.findAll(toPredicate(parameters))
      .stream()
      .map(GatewayWithMetersMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<Gateway> findAll(RequestParameters parameters, Pageable pageable) {
    org.springframework.data.domain.Page<GatewayEntity> all = repository.findAll(
      toPredicate(parameters),
      new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
    );

    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap = statusLogJpaRepository
      .findAllGroupedByGatewayId(toStatusPredicate(all.getContent(), parameters));

    return new PageAdapter<>(
      all.map((entity) -> GatewayWithMetersMapper.toDomainModel(entity, statusLogMap))
    );
  }

  @Transactional
  @Override
  public List<Gateway> findAllByOrganisationId(UUID organisationId) {
    List<GatewayEntity> gateways = repository.findAllByOrganisationId(organisationId);

    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap = statusLogJpaRepository
      .findAllGroupedByGatewayId(toStatusPredicate(gateways));

    return toGateways(gateways, statusLogMap);
  }

  @Override
  public Gateway save(Gateway gateway) {
    GatewayEntity entity = repository.save(GatewayEntityMapper.toEntity(gateway));
    return GatewayEntityMapper.toDomainModel(entity);
  }

  @Override
  public Optional<Gateway> findBy(UUID organisationId, String productModel, String serial) {
    return repository.findByOrganisationIdAndProductModelAndSerial(
      organisationId,
      productModel,
      serial
    ).map(GatewayEntityMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findBy(UUID organisationId, String serial) {
    return repository.findByOrganisationIdAndSerial(
      organisationId,
      serial
    ).map(GatewayEntityMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findById(UUID id) {
    return repository.findById(id).map(GatewayWithMetersMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return repository.findByOrganisationIdAndId(organisationId, id)
      .map(GatewayWithMetersMapper::toDomainModel);
  }

  private List<Gateway> toGateways(
    List<GatewayEntity> entities,
    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap
  ) {
    return entities
      .stream()
      .map(gateway -> GatewayWithMetersMapper.toDomainModel(gateway, statusLogMap))
      .collect(toList());
  }

  private Predicate toStatusPredicate(List<GatewayEntity> content) {
    return toStatusPredicate(content, new RequestParametersAdapter());
  }

  private Predicate toStatusPredicate(
    List<GatewayEntity> content,
    RequestParameters parameters
  ) {
    RequestParameters newParameters = parameters.shallowCopy();
    newParameters.setAll("gatewayId", getGatewayIds(content));

    return gatewayStatusLogQueryFilters.toExpression(newParameters);
  }

  private Predicate toPredicate(RequestParameters parameters) {
    return gatewayQueryFilters.toExpression(parameters);
  }

  private List<String> getGatewayIds(List<GatewayEntity> gatewayEntities) {
    return gatewayEntities.stream()
      .map(gatewayEntity -> gatewayEntity.id.toString())
      .collect(toList());
  }
}
