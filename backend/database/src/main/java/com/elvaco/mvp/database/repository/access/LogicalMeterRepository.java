package com.elvaco.mvp.database.repository.access;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PagedLogicalMeter;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j
public class LogicalMeterRepository implements LogicalMeters {

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final LogicalMeterSortingEntityMapper sortingMapper;

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return logicalMeterJpaRepository.findById(id)
      .flatMap(this::filterParametersOn);
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return logicalMeterJpaRepository.findBy(organisationId, id)
      .flatMap(this::filterParametersOn);
  }

  @Override
  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
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

    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return new PageAdapter<>(pagedLogicalMeters.map(LogicalMeterEntityMapper::toDomainModel));
    }
    ZonedDateTime after = ZonedDateTime.parse(parameters.getFirst("after"));
    ZonedDateTime before = ZonedDateTime.parse(parameters.getFirst("before"));
    return new PageAdapter<>(
      pagedLogicalMeters.map(source ->
        LogicalMeterEntityMapper.toDomainModelWithCollectionPercentage(
          source, source.expectedMeasurementCount(after, before)
        )
      ));
  }

  @Override
  public List<LogicalMeter> findAll(RequestParameters parameters) {
    List<LogicalMeterEntity> meters = sortingMapper.getAsSpringSort(parameters)
      .map(sort -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters), sort))
      .orElseGet(() -> logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters)));

    return mapAndCollectWithStatuses(meters, parameters);
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
    return logicalMeterJpaRepository.findBy(organisationId, externalId)
      .map(LogicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public List<LogicalMeter> findByOrganisationId(UUID organisationId) {
    return logicalMeterJpaRepository.findByOrganisationId(organisationId)
      .stream()
      .map(LogicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    return logicalMeterJpaRepository.summary(parameters, toPredicate(parameters));
  }

  @Transactional
  @Override
  public void delete(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.delete(
      logicalMeter.id,
      logicalMeter.organisationId
    );
  }

  private Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }

  private Iterable<PhysicalMeterStatusLogEntity> getStatusesForMeters(
    RequestParameters parameters
  ) {
    return physicalMeterStatusLogJpaRepository.findAll(
      new PhysicalMeterStatusLogQueryFilters().toExpression(parameters),
      new Sort(
        Direction.DESC,
        toSortString(STATUS_LOG.start),
        toSortString(STATUS_LOG.stop)
      )
    );
  }

  private Optional<LogicalMeter> filterParametersOn(LogicalMeterEntity meter) {
    return mapAndCollectWithStatuses(singletonList(meter), new RequestParametersAdapter())
      .stream()
      .findFirst();
  }

  private List<LogicalMeter> mapAndCollectWithStatuses(
    List<LogicalMeterEntity> meters,
    RequestParameters parameters
  ) {
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses =
      getStatusesGroupedByPhysicalMeterId(getStatusesForMeters(parameters));

    Map<UUID, Long> meterCounts = getCountForMetersWithinPeriod(parameters);

    return meters
      .stream()
      .map(logicalMeterEntity -> LogicalMeterEntityMapper.toDomainModel(
        logicalMeterEntity,
        mappedStatuses,
        meterCounts
      ))
      .collect(toList());
  }

  private Map<UUID, List<PhysicalMeterStatusLogEntity>> getStatusesGroupedByPhysicalMeterId(
    Iterable<PhysicalMeterStatusLogEntity> statuses
  ) {
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses = new HashMap<>();

    statuses.forEach(status -> {
      if (!mappedStatuses.containsKey(status.physicalMeterId)) {
        mappedStatuses.put(status.physicalMeterId, new ArrayList<>());
      }
      mappedStatuses.get(status.physicalMeterId).add(status);
    });
    return mappedStatuses;
  }

  private Map<UUID, Long> getCountForMetersWithinPeriod(
    RequestParameters parameters
  ) {
    return logicalMeterJpaRepository.findMeasurementCounts(
      new MeasurementQueryFilters().toExpression(parameters)
    );
  }

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("physicalMeterStatusLogEntity.", "");
  }
}
