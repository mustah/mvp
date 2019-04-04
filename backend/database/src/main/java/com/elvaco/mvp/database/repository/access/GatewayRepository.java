package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayEntityMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;

import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.getSortOrUnsorted;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository gatewayJpaRepository;
  private final GatewayWithMetersMapper gatewayWithMetersMapper;

  @Override
  public List<Gateway> findAll() {
    return gatewayJpaRepository.findAll().stream()
      .map(gatewayWithMetersMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<GatewaySummaryDto> findAll(
    RequestParameters parameters,
    Pageable pageable
  ) {
    PageRequest pageRequest = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      getSortOrUnsorted(parameters)
    );
    org.springframework.data.domain.Page<GatewaySummaryDto> pagedGateways =
      gatewayJpaRepository.findAll(parameters, pageRequest);

    return new PageAdapter<>(
      pagedGateways
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
  public List<Gateway> findBy(String serial) {
    return gatewayJpaRepository.findBySerial(serial).stream()
      .map(GatewayEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Gateway> findById(UUID id) {
    return gatewayJpaRepository.findById(id).map(gatewayWithMetersMapper::toDomainModel);
  }

  @Override
  public Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return gatewayJpaRepository.findByOrganisationIdAndId(organisationId, id)
      .map(gatewayWithMetersMapper::toDomainModel);
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
}
