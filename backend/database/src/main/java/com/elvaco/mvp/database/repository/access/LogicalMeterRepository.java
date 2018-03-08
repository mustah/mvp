package com.elvaco.mvp.database.repository.access;

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
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.queryfilters.QueryFilters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class LogicalMeterRepository implements LogicalMeters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LogicalMeterMapper logicalMeterMapper;
  private final LogicalMeterSortingMapper sortingMapper;
  private final QueryFilters meterQueryFilters;
  private final QueryFilters statusQueryFilters;

  public LogicalMeterRepository(
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    LogicalMeterSortingMapper sortingMapper,
    LogicalMeterMapper logicalMeterMapper,
    QueryFilters meterQueryFilters,
    QueryFilters statusQueryFilters
  ) {
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.sortingMapper = sortingMapper;
    this.logicalMeterMapper = logicalMeterMapper;
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.meterQueryFilters = meterQueryFilters;
    this.statusQueryFilters = statusQueryFilters;
  }

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return logicalMeterJpaRepository.findById(id)
      .flatMap(this::filterParametersOn);
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return logicalMeterJpaRepository.findByOrganisationIdAndId(organisationId, id)
      .flatMap(this::filterParametersOn);
  }

  @Override
  public List<LogicalMeter> findAll() {
    List<LogicalMeterEntity> all = logicalMeterJpaRepository.findAll();
    return mapAndCollectWithStatuses(all, new RequestParametersAdapter());
  }

  @Override
  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    org.springframework.data.domain.Page<LogicalMeterEntity> logicalMeterEntities =
      logicalMeterJpaRepository.findAll(
        meterQueryFilters.toExpression(parameters),
        new PageRequest(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          sortingMapper.getAsSpringSort(pageable.getSort())
        )
      );

    return new PageAdapter<>(
      logicalMeterEntities.map(
        logicalMeter -> logicalMeterMapper.toDomainModel(
          logicalMeter,
          getStatusesGroupedByPhysicalMeterId(
            getStatusesForMeters(logicalMeterEntities.getContent(), parameters)
          )
        )
      )
    );
  }

  @Override
  public List<LogicalMeter> findAll(RequestParameters parameters) {
    List<LogicalMeterEntity> all =
      logicalMeterJpaRepository.findAll(meterQueryFilters.toExpression(parameters));
    return mapAndCollectWithStatuses(all, parameters);
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    LogicalMeterEntity entity = logicalMeterMapper.toEntity(logicalMeter);
    return logicalMeterMapper.toDomainModel(logicalMeterJpaRepository.save(entity));
  }

  @Override
  public void deleteAll() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return logicalMeterJpaRepository
      .findByOrganisationIdAndExternalId(organisationId, externalId)
      .map(logicalMeterMapper::toDomainModel);
  }

  @Override
  public List<LogicalMeter> findByOrganisationId(UUID organisationId) {
    return logicalMeterJpaRepository.findByOrganisationId(organisationId)
      .stream()
      .map(logicalMeterMapper::toDomainModel)
      .collect(toList());
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

    parameters.setAll("physicalMeterIds", physicalMeterIds);

    return physicalMeterStatusLogJpaRepository.findAll(
      statusQueryFilters.toExpression(parameters),
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

    return meters
      .stream()
      .map(entity -> logicalMeterMapper.toDomainModel(entity, mappedStatuses))
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

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("physicalMeterStatusLogEntity.", "");
  }
}
