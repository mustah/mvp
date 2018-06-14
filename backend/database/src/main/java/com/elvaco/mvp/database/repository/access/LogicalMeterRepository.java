package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
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
import com.elvaco.mvp.database.repository.queryfilters.MeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j
public class LogicalMeterRepository implements LogicalMeters {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final SummaryJpaRepository summaryJpaRepository;
  private final LogicalMeterSortingEntityMapper sortingMapper;

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return findOneBy(new RequestParametersAdapter().replace("id", id.toString()));
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return findOneBy(
      new RequestParametersAdapter()
        .replace("id", id.toString())
        .replace("organisation", organisationId.toString())
    );
  }

  @Override
  public Page<LogicalMeter> findAllWithStatuses(RequestParameters parameters, Pageable pageable) {
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

    return parameters.getAsSelectionPeriod("after", "before")
      .map(selectionPeriod ->
        new PageAdapter<>(
          pagedLogicalMeters.map(source ->
            LogicalMeterEntityMapper.toDomainModelWithCollectionPercentage(
              source,
              source.expectedMeasurementCount(selectionPeriod)
            )
          )
        )
      )
      .orElse(
        new PageAdapter<>(pagedLogicalMeters.map(LogicalMeterEntityMapper::toDomainModel))
      );
  }

  @Override
  public List<LogicalMeter> findAllWithStatuses(RequestParameters parameters) {
    List<LogicalMeterEntity> meters = sortingMapper.getAsSpringSort(parameters)
      .map(sort -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters), sort))
      .orElseGet(() -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters)));

    return mapAndCollectWithStatuses(meters, parameters);
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
  public LogicalMeter save(LogicalMeter logicalMeter) {
    LogicalMeterEntity entity = LogicalMeterEntityMapper.toEntity(logicalMeter);
    return LogicalMeterEntityMapper.toDomainModel(logicalMeterJpaRepository.save(entity));
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return logicalMeterJpaRepository.findOneBy(organisationId, externalId)
      .map(LogicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    return summaryJpaRepository.summary(parameters, toPredicate(parameters));
  }

  @Transactional
  @Override
  public void delete(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.delete(
      logicalMeter.id,
      logicalMeter.organisationId
    );
  }

  @Override
  public Optional<LogicalMeter> findOneBy(RequestParameters parameters) {
    return logicalMeterJpaRepository.findOneBy(parameters)
      .map(LogicalMeterEntityMapper::toDomainModel);
  }

  private List<LogicalMeter> mapAndCollectWithStatuses(
    List<LogicalMeterEntity> meters,
    RequestParameters parameters
  ) {
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses =
      logicalMeterJpaRepository.findStatusesGroupedByPhysicalMeterId(
        new PhysicalMeterStatusLogQueryFilters().toExpression(parameters)
      );

    return parameters.getAsSelectionPeriod("after", "before")
      .map(selectionPeriod ->
        withStatusesAndCollectionStats(meters, parameters, mappedStatuses, selectionPeriod)
      )
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
    List<LogicalMeterEntity> meters,
    RequestParameters parameters,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses,
    SelectionPeriod selectionPeriod
  ) {
    Map<UUID, Long> countForMetersWithinPeriod = getCountForMetersWithinPeriod(parameters);
    return meters
      .stream()
      .map(logicalMeterEntity -> {
        Long expectedMeasurementCount = (long) LogicalMeterHelper.calculateExpectedReadOuts(
          logicalMeterEntity.physicalMeters.stream()
            .findFirst()
            .map(physicalMeterEntity -> physicalMeterEntity.readIntervalMinutes)
            .orElse(0L),
          selectionPeriod
        ) * logicalMeterEntity.meterDefinition.quantities.size();
        return LogicalMeterEntityMapper.toDomainModel(
          logicalMeterEntity,
          mappedStatuses,
          expectedMeasurementCount,
          countForMetersWithinPeriod.getOrDefault(logicalMeterEntity.id, 0L)
        );
      })
      .collect(toList());
  }

  private Map<UUID, Long> getCountForMetersWithinPeriod(
    RequestParameters parameters
  ) {
    return logicalMeterJpaRepository.findMeasurementCounts(
      new MeasurementQueryFilters().toExpression(parameters)
    );
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
