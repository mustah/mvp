package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.SummaryJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.SortUtil;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper.toDomainModelWithCollectionPercentage;
import static com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper.toDomainModelWithoutStatuses;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class LogicalMeterRepository implements LogicalMeters {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final LogicalMeterSortingEntityMapper sortingMapper;

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return logicalMeterJpaRepository.findById(id)
      .map(LogicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return logicalMeterJpaRepository.findByOrganisationIdAndId(organisationId, id)
      .map(LogicalMeterEntityMapper::toDomainModel);
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
      .map(LogicalMeterEntityMapper::toDomainModelWithoutStatuses);
  }

  @Override
  public Optional<LogicalMeter> findBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findBy(parameters)
      .map(LogicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public Page<LogicalMeter> findAll(
    RequestParameters parameters,
    Pageable pageable
  ) {
    org.springframework.data.domain.Page<PagedLogicalMeter> pagedLogicalMeters =
      logicalMeterJpaRepository.findAll(
        parameters,
        toPredicate(parameters),
        new PageRequest(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          sortingMapper.getAsSpringSort(pageable.getSort())
        )
      );

    return parameters.getAsSelectionPeriod(AFTER, BEFORE).map(selectionPeriod ->
      new PageAdapter<>(
        pagedLogicalMeters.map(source ->
          toDomainModelWithCollectionPercentage(
            source,
            source.expectedReadingCount(selectionPeriod)
          )
        )
      )
    ).orElse(new PageAdapter<>(pagedLogicalMeters.map(LogicalMeterEntityMapper::toDomainModel)));
  }

  @Override
  public List<LogicalMeter> findAllWithStatuses(RequestParameters parameters) {
    List<LogicalMeterEntity> meters = SortUtil.getSort(parameters)
      .map(sort -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters), sort))
      .orElseGet(() -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters)));

    return findAllWithCollectionStatsAndStatuses(meters, parameters);
  }

  @Override
  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters)).stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllByOrganisationId(UUID organisationId) {
    return logicalMeterJpaRepository.findByOrganisationId(organisationId).stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllForSelectionTree(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAllForSelectionTree(parameters).stream()
      .map(LogicalMeterEntityMapper::toDomainModelWithLocation)
      .collect(toList());
  }

  @Override
  @CacheEvict(
    cacheNames = "logicalMeter.organisationIdExternalId",
    key = "#logicalMeter.organisationId + #logicalMeter.externalId"
  )
  public LogicalMeter save(LogicalMeter logicalMeter) {
    LogicalMeterEntity entity = LogicalMeterEntityMapper.toEntity(logicalMeter);
    return toDomainModelWithoutStatuses(logicalMeterJpaRepository.save(entity));
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    return summaryJpaRepository.summary(parameters);
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeasurements(RequestParameters parameters) {
    return parameters.getAsSelectionPeriod(AFTER, BEFORE)
      .map(selectionPeriod ->
        logicalMeterJpaRepository.findMissingMeterReadingsCounts(parameters).stream()
          .map(entry -> LogicalMeterEntityMapper.toDomainModel(entry, selectionPeriod))
          .collect(toList()))
      .orElse(emptyList());
  }

  @Transactional
  @Override
  public void delete(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.delete(logicalMeter.id, logicalMeter.organisationId);
  }

  private List<LogicalMeter> findAllWithCollectionStatsAndStatuses(
    List<LogicalMeterEntity> meters,
    RequestParameters parameters
  ) {
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses =
      logicalMeterJpaRepository.findStatusesGroupedByPhysicalMeterId(parameters);

    return parameters.getAsSelectionPeriod(AFTER, BEFORE)
      .map(selectionPeriod -> withStatusesAndCollectionStats(
        meters,
        parameters,
        mappedStatuses,
        selectionPeriod
      ))
      .orElse(withStatusesOnly(meters, mappedStatuses));
  }

  private List<LogicalMeter> withStatusesOnly(
    List<LogicalMeterEntity> meters,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses
  ) {
    return meters.stream()
      .map(entity -> LogicalMeterEntityMapper.toDomainModel(entity, mappedStatuses))
      .collect(toList());
  }

  private List<LogicalMeter> withStatusesAndCollectionStats(
    List<LogicalMeterEntity> logicalMeters,
    RequestParameters parameters,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses,
    SelectionPeriod selectionPeriod
  ) {
    Map<UUID, Long> missingMeasurementsCount =
      getMissingCountForMetersWithinPeriod(parameters);

    return logicalMeters.stream()
      .map(logicalMeterEntity -> LogicalMeterEntityMapper.toDomainModel(
        logicalMeterEntity,
        mappedStatuses,
        getReadoutCount(selectionPeriod, logicalMeterEntity),
        missingMeasurementsCount.getOrDefault(logicalMeterEntity.id, 0L)
      ))
      .collect(toList());
  }

  private long getReadoutCount(
    SelectionPeriod selectionPeriod,
    LogicalMeterEntity logicalMeterEntity
  ) {
    Long readIntervalMinutes = logicalMeterEntity.physicalMeters.stream()
      .findFirst()
      .map(physicalMeterEntity -> physicalMeterEntity.readIntervalMinutes)
      .orElse(0L);
    return LogicalMeterHelper.calculateExpectedReadOuts(readIntervalMinutes, selectionPeriod);
  }

  private Map<UUID, Long> getMissingCountForMetersWithinPeriod(
    RequestParameters parameters
  ) {
    return logicalMeterJpaRepository.findMissingMeterReadingsCounts(parameters).stream()
      .collect(toMap(entry -> entry.id, entry -> entry.missingReadingCount));
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
