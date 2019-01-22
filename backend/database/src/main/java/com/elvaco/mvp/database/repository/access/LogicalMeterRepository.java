package com.elvaco.mvp.database.repository.access;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SummaryJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingEntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class LogicalMeterRepository implements LogicalMeters {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final LogicalMeterSortingEntityMapper sortingMapper;
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
    key = "#organisationId + #externalId"
  )
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return logicalMeterJpaRepository.findBy(organisationId, externalId)
      .map(logicalMeterEntityMapper::toDomainModelWithoutStatuses);
  }

  @Override
  public Optional<LogicalMeter> findBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findBy(parameters)
      .map(logicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findSecondaryAddresses(
        parameters,
        PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          sortingMapper.getAsQSort(pageable.getSort())
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
          sortingMapper.getAsQSort(pageable.getSort())
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
          sortingMapper.getAsQSort(pageable.getSort())
        )
      )
    );
  }

  @Override
  public List<LogicalMeter> findAllWithDetails(RequestParameters parameters) {
    Collection<LogicalMeterEntity> meters = logicalMeterJpaRepository.findAll(parameters);

    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses = meters.stream()
      .flatMap(lm -> lm.physicalMeters.stream())
      .collect(groupingBy(
        PhysicalMeterEntity::getId,
        flatMapping(pm -> pm.statusLogs.stream(), toList())
      ));

    return parameters.getPeriod()
      .map(ignore -> withStatusesAndCollectionStats(
        meters,
        parameters,
        mappedStatuses
      ))
      .orElse(withStatusesOnly(meters, mappedStatuses));
  }

  @Override
  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAll(parameters).stream()
      .map(logicalMeterEntityMapper::toSimpleDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllByOrganisationId(UUID organisationId) {
    return logicalMeterJpaRepository.findByOrganisationId(organisationId).stream()
      .map(logicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllForSelectionTree(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAllForSelectionTree(parameters).stream()
      .map(logicalMeterEntityMapper::toDomainModelWithLocation)
      .collect(toList());
  }

  @Override
  @CacheEvict(
    cacheNames = "logicalMeter.organisationIdExternalId",
    key = "#logicalMeter.organisationId + #logicalMeter.externalId"
  )
  public LogicalMeter save(LogicalMeter logicalMeter) {
    return logicalMeterEntityMapper.toDomainModelWithoutStatuses(
      logicalMeterJpaRepository.save(logicalMeterEntityMapper.toEntity(logicalMeter))
    );
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    return summaryJpaRepository.summary(parameters);
  }

  @Override
  public List<LogicalMeterCollectionStats> findMeterCollectionStats(
    RequestParameters parameters
  ) {
    return parameters.getPeriod()
      .map(ignore -> logicalMeterJpaRepository.findMeterCollectionStats(parameters))
      .orElse(emptyList());
  }

  @Transactional
  @Override
  public LogicalMeter delete(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.delete(logicalMeter.id, logicalMeter.organisationId);
    return logicalMeter;
  }

  private List<LogicalMeter> withStatusesOnly(
    Collection<LogicalMeterEntity> meters,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses
  ) {
    return meters.stream()
      .map(entity -> logicalMeterEntityMapper.toDomainModel(entity, mappedStatuses, null))
      .collect(toList());
  }

  private List<LogicalMeter> withStatusesAndCollectionStats(
    Collection<LogicalMeterEntity> logicalMeters,
    RequestParameters parameters,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses
  ) {
    Map<UUID, Double> collectionPercentages =
      getCollectionPercentagesForMeters(parameters);

    return logicalMeters.stream()
      .map(logicalMeterEntity -> logicalMeterEntityMapper.toDomainModel(
        logicalMeterEntity,
        mappedStatuses,
        collectionPercentages.getOrDefault(logicalMeterEntity.getLogicalMeterId(), 0.0)
      ))
      .collect(toList());
  }

  private Map<UUID, Double> getCollectionPercentagesForMeters(
    RequestParameters parameters
  ) {
    return logicalMeterJpaRepository.findMeterCollectionStats(parameters).stream()
      .collect(toMap(
        entry -> entry.id,
        entry -> entry.collectionPercentage,
        (oldPct, newPct) -> oldPct + newPct
      ));
  }
}
