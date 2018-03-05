package com.elvaco.mvp.database.repository.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterStatusLogToPredicateMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class LogicalMeterRepository implements LogicalMeters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LogicalMeterMapper logicalMeterMapper;
  private final LogicalMeterToPredicateMapper filterMapper;
  private final LogicalMeterSortingMapper sortingMapper;
  private final PhysicalMeterStatusLogToPredicateMapper statusFilterMapper;

  public LogicalMeterRepository(
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    LogicalMeterToPredicateMapper filterMapper,
    LogicalMeterSortingMapper sortingMapper,
    LogicalMeterMapper logicalMeterMapper,
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    PhysicalMeterStatusLogToPredicateMapper statusFilterMapper
  ) {
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.filterMapper = filterMapper;
    this.sortingMapper = sortingMapper;
    this.logicalMeterMapper = logicalMeterMapper;
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.statusFilterMapper = statusFilterMapper;
  }

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return mapAndCollectWithStatuses(logicalMeterJpaRepository.findById(id));
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return mapAndCollectWithStatuses(logicalMeterJpaRepository
      .findByOrganisationIdAndId(organisationId, id));
  }

  @Override
  public List<LogicalMeter> findAll() {
    List<LogicalMeterEntity> all = logicalMeterJpaRepository.findAll();
    return mapAndCollectWithStatuses(all, emptyMap());
  }

  @Override
  public List<LogicalMeter> findAll(Map<String, List<String>> filterParams) {
    List<LogicalMeterEntity> all = logicalMeterJpaRepository.findAll(
      filterMapper.map(filterParams)
    );

    return mapAndCollectWithStatuses(all, filterParams);
  }

  @Override
  public Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    org.springframework.data.domain.Page<LogicalMeterEntity> logicalMeterEntities =
      logicalMeterJpaRepository.findAll(
        filterMapper.map(filterParams),
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
            getStatusesForMeters(logicalMeterEntities.getContent(), filterParams)
          )
        )
      )
    );
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = logicalMeterMapper.toEntity(logicalMeter);
    return logicalMeterMapper.toDomainModel(
      logicalMeterJpaRepository.save(logicalMeterEntity)
    );
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
    Map<String, List<String>> filterParams) {
    List<UUID> physicalMeterIds = new ArrayList<>();
    logicalMeterEntities.forEach(
      logicalMeterEntity -> logicalMeterEntity.physicalMeters.forEach(
        physicalMeterEntity -> physicalMeterIds.add(physicalMeterEntity.id)
      )
    );

    return physicalMeterStatusLogJpaRepository.findAll(
      statusFilterMapper.map(physicalMeterIds, filterParams),
      new Sort(Direction.DESC,
        toSortString(Q.start),
        toSortString(Q.stop))
    );
  }

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("physicalMeterStatusLogEntity.", "");
  }

  private Optional<LogicalMeter> mapAndCollectWithStatuses(
    Optional<LogicalMeterEntity> logicalMeterEntity
  ) {
    return logicalMeterEntity
      .flatMap(meter -> mapAndCollectWithStatuses(asList(meter), emptyMap()).stream().findFirst());
  }

  private List<LogicalMeter> mapAndCollectWithStatuses(
    List<LogicalMeterEntity> meters,
    Map<String, List<String>> filterParams
  ) {
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses =
      getStatusesGroupedByPhysicalMeterId(
        getStatusesForMeters(meters, filterParams)
      );

    return meters
      .stream()
      .map(logicalMeterEntity -> logicalMeterMapper.toDomainModel(
        logicalMeterEntity,
        mappedStatuses
        )
      ).collect(toList());
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
}
