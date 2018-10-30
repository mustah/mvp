package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.gateway.PagedGateway;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayEntityMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.queryfilters.GatewayStatusLogQueryFilters;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;

import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_ID;
import static com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper.ofPageableDomainModel;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.getSortOrUnsorted;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository gatewayJpaRepository;
  private final GatewayStatusLogJpaRepository statusLogJpaRepository;

  @Override
  public List<Gateway> findAll(RequestParameters parameters) {
    return gatewayJpaRepository.findAll(parameters).stream()
      .map(GatewayWithMetersMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<Gateway> findAll(
    RequestParameters parameters,
    Pageable pageable
  ) {
    PageRequest pageRequest = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      getSortOrUnsorted(parameters)
    );
    org.springframework.data.domain.Page<PagedGateway> pagedGateways =
      gatewayJpaRepository.findAll(parameters, pageRequest);

    Map<UUID, List<GatewayStatusLogEntity>> statusLogMap = statusLogJpaRepository
      .findAllGroupedByGatewayId(toStatusPredicate(pagedGateways.getContent(), parameters));

    return new PageAdapter<>(
      pagedGateways.map(pagedGateway -> ofPageableDomainModel(pagedGateway, statusLogMap))
    );
  }

  @Override
  @CacheEvict(
    cacheNames = "gateway.organisationIdSerial",
    key = "#gateway.organisationId + #gateway.serial"
  )
  public Gateway save(Gateway gateway) {
    GatewayEntity entity = gatewayJpaRepository.save(GatewayEntityMapper.toEntity(gateway));
    return GatewayEntityMapper.toDomainModelWithoutStatusLogs(entity);
  }

  @Override
  public Optional<Gateway> findBy(UUID organisationId, String productModel, String serial) {
    return gatewayJpaRepository.findByOrganisationIdAndProductModelAndSerial(
      organisationId,
      productModel,
      serial
    ).map(GatewayEntityMapper::toDomainModel);
  }

  @Override
  @Cacheable(
    cacheNames = "gateway.organisationIdSerial",
    key = "#organisationId + #serial"
  )
  public Optional<Gateway> findBy(UUID organisationId, String serial) {
    return gatewayJpaRepository.findByOrganisationIdAndSerial(organisationId, serial)
      .map(GatewayEntityMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findById(UUID id) {
    return gatewayJpaRepository.findById(id).map(GatewayWithMetersMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return gatewayJpaRepository.findByOrganisationIdAndId(organisationId, id)
      .map(GatewayWithMetersMapper::toDomainModel);
  }

  @Override
  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(gatewayJpaRepository.findSerials(
      parameters,
      PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        getSortOrUnsorted(parameters)
      )
    ));
  }

  @Nullable
  private static Predicate toStatusPredicate(
    List<? extends Identifiable<UUID>> identifiables, RequestParameters parameters
  ) {
    List<String> gatewayIds = identifiables.stream()
      .map(entity -> entity.getId().toString())
      .collect(toList());
    return new GatewayStatusLogQueryFilters()
      .toExpression(parameters.shallowCopy().setAll(GATEWAY_ID, gatewayIds));
  }
}
