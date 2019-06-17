package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SummaryJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.SortMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LogicalMeterRepository implements LogicalMeters {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final LogicalMeterEntityMapper logicalMeterEntityMapper;

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return logicalMeterJpaRepository.findById(id)
      .map(logicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public Optional<LogicalMeter> findByPrimaryKey(UUID organisationId, UUID id) {
    return logicalMeterJpaRepository.findByPrimaryKey(organisationId, id)
      .map(logicalMeterEntityMapper::toDomainModel);
  }

  @Override
  @Cacheable(
    cacheNames = "logicalMeter.organisationIdExternalId",
    key = "#organisationId + #externalId",
    sync = true
  )
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return logicalMeterJpaRepository.findBy(organisationId, externalId)
      .map(logicalMeterEntityMapper::toDomainModelWithoutStatuses);
  }

  @Override
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findSecondaryAddresses(
        parameters,
        PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          SortMapper.getAsSpringSort(pageable.getSort())
        )
      ));
  }

  @Override
  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findFacilities(
        parameters,
        PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          SortMapper.getAsSpringSort(pageable.getSort())
        )
      ));
  }

  @Override
  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findAll(
        parameters,
        PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          SortMapper.getAsSpringSort(pageable.getSort())
        )
      )
    );
  }

  @Override
  public List<LegendDto> findAllLegendsBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAllLegends(parameters);
  }

  @Override
  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAll(parameters).stream()
      .map(logicalMeterEntityMapper::toSimpleDomainModel)
      .collect(toList());
  }

  @Override
  @Caching(evict = {
    @CacheEvict(
      cacheNames = "logicalMeter.organisationIdExternalId",
      key = "#logicalMeter.organisationId + #logicalMeter.externalId"
    ),
    @CacheEvict(
      cacheNames = {"gateway.organisationIdSerial"},
      allEntries = true
    )
  })
  public LogicalMeter save(LogicalMeter logicalMeter) {
    return logicalMeterEntityMapper.toDomainModelWithoutStatuses(
      logicalMeterJpaRepository.save(logicalMeterEntityMapper.toEntity(logicalMeter))
    );
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    Filters filters = toFilters(parameters);
    return new MeterSummary(
      summaryJpaRepository.meterCount(filters),
      summaryJpaRepository.cityCount(filters),
      summaryJpaRepository.addressCount(filters)
    );
  }

  @Override
  public long meterCount(RequestParameters parameters) {
    return summaryJpaRepository.meterCount(toFilters(parameters));
  }

  @Transactional
  @Override
  @Caching(evict = {
    @CacheEvict(
      value = "logicalMeter.organisationIdExternalId",
      key = "#logicalMeter.organisationId + #logicalMeter.externalId"
    ),
    @CacheEvict(
      value = {"physicalMeter.organisationIdExternalIdAddress",
               "physicalMeter.organisationIdExternalIdAddress.withStatuses",
               "gateway.organisationIdSerial"},
      allEntries = true
    )
  })
  public LogicalMeter delete(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.delete(logicalMeter.id, logicalMeter.organisationId);
    return logicalMeter;
  }

  @Override
  @CacheEvict(
    cacheNames = {"logicalMeter.organisationIdExternalId",
                  "gateway.organisationIdSerial"},
    allEntries = true
  )
  @Transactional
  public void changeMeterDefinition(
    UUID organisationId,
    MeterDefinition fromMeterDefinition,
    MeterDefinition toMeterDefinition
  ) {
    logicalMeterJpaRepository.changeMeterDefinition(
      organisationId,
      fromMeterDefinition.id,
      toMeterDefinition.id
    );
  }

  @Override
  public List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters) {
    return logicalMeterJpaRepository.getPreferredQuantityParameters(parameters);
  }
}
