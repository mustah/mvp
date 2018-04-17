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
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LogicalMeterRepository implements LogicalMeters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final LogicalMeterSortingMapper sortingMapper;

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("physicalMeterStatusLogEntity.", "");
  }

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
    org.springframework.data.domain.Page<LogicalMeterEntity> logicalMeterEntities =
      logicalMeterJpaRepository.findAll(
        parameters,
        toPredicate(parameters),
        new PageRequest(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          sortingMapper.getAsSpringSort(pageable.getSort())
        )
      );
    Iterable<PhysicalMeterStatusLogEntity> statusLogEntities = getStatusesForMeters(
      logicalMeterEntities.getContent(),
      parameters
    );

    Map<UUID, List<PhysicalMeterStatusLogEntity>> mapStatus = getStatusesGroupedByPhysicalMeterId(
      statusLogEntities
    );

    Map<UUID, Long> mapMeasurementCount = getCountForMetersWithinPeriod(
      logicalMeterEntities.getContent(),
      parameters
    );

    Page<LogicalMeter> page = new PageAdapter<>(
      logicalMeterEntities.map(
        logicalMeter -> LogicalMeterMapper.toDomainModel(
          logicalMeter, mapStatus, mapMeasurementCount
        )
      ));

    return page;
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
    LogicalMeterEntity entity = LogicalMeterMapper.toEntity(logicalMeter);
    return LogicalMeterMapper.toDomainModel(logicalMeterJpaRepository.save(entity));
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return logicalMeterJpaRepository.findBy(organisationId, externalId)
      .map(LogicalMeterMapper::toDomainModel);
  }

  @Override
  public List<LogicalMeter> findByOrganisationId(UUID organisationId) {
    return logicalMeterJpaRepository.findByOrganisationId(organisationId)
      .stream()
      .map(LogicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllForSummaryInfo(RequestParameters parameters) {
    return logicalMeterJpaRepository.findAll(parameters, toPredicate(parameters))
      .stream()
      .map(LogicalMeterMapper::justLocationModel)
      .collect(toList());
  }

  private Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }

  private Iterable<PhysicalMeterStatusLogEntity> getStatusesForMeters(
    List<LogicalMeterEntity> logicalMeterEntities,
    RequestParameters parameters
  ) {
    List<String> physicalMeterIds = logicalMeterEntities
      .stream()
      .flatMap(logicalMeterEntity -> logicalMeterEntity.physicalMeters.stream())
      .map(entity -> entity.getId().toString())
      .collect(toList());

    parameters.setAll("physicalMeterId", physicalMeterIds);

    return physicalMeterStatusLogJpaRepository.findAll(
      new PhysicalMeterStatusLogQueryFilters().toExpression(parameters),
      new Sort(
        Direction.DESC,
        toSortString(Q.start),
        toSortString(Q.stop)
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
      getStatusesGroupedByPhysicalMeterId(getStatusesForMeters(meters, parameters));

    Map<UUID, Long> meterCounts = getCountForMetersWithinPeriod(meters, parameters);

    return meters
      .stream()
      .map(logicalMeterEntity -> LogicalMeterMapper.toDomainModel(
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
    List<LogicalMeterEntity> logicalMeterEntities,
    RequestParameters parameters
  ) {
    if (parameters.hasName("after") && parameters.hasName("before")) {
      List<UUID> physicalMeterIds = logicalMeterEntities
        .stream()
        .flatMap(logicalMeterEntity -> logicalMeterEntity.physicalMeters.stream())
        .map(PhysicalMeterEntity::getId)
        .collect(toList());

      if (!physicalMeterIds.isEmpty()) {
        // TODO handle multiple dates?
        return getCountForMetersWithinPeriod(
          physicalMeterIds,
          ZonedDateTime.parse(parameters.getFirst("after")),
          ZonedDateTime.parse(parameters.getFirst("before"))
        );
      }
    }
    return emptyMap();
  }

  private Map<UUID, Long> getCountForMetersWithinPeriod(
    List<UUID> physicalMeterIds,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    QMeasurementEntity q = QMeasurementEntity.measurementEntity;
    // TODO check that all measurements matches interval pattern
    return measurementJpaRepository.countGroupedByPhysicalMeterId(
      q.physicalMeter.id.in(physicalMeterIds)
        .and(q.created.goe(after)) // Roughly filter on date
        .and(q.created.before(before))
    );
  }
}
