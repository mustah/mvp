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
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;
import org.springframework.data.domain.PageRequest;

import static java.util.stream.Collectors.toList;

public class LogicalMeterRepository implements LogicalMeters {

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LogicalMeterMapper logicalMeterMapper;
  private final LogicalMeterToPredicateMapper filterMapper;
  private final LogicalMeterSortingMapper sortingMapper;

  public LogicalMeterRepository(
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    LogicalMeterToPredicateMapper filterMapper,
    LogicalMeterSortingMapper sortingMapper,
    LogicalMeterMapper logicalMeterMapper,
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository
  ) {
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.filterMapper = filterMapper;
    this.sortingMapper = sortingMapper;
    this.logicalMeterMapper = logicalMeterMapper;
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
  }

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return logicalMeterJpaRepository.findById(id).map(logicalMeterMapper::toDomainModel);
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return logicalMeterJpaRepository
      .findByOrganisationIdAndId(organisationId, id)
      .map(logicalMeterMapper::toDomainModel);
  }

  @Override
  public List<LogicalMeter> findAll() {
    List<LogicalMeterEntity> all = logicalMeterJpaRepository.findAll();
    return mapAndCollect(all);
  }

  @Override
  public List<LogicalMeter> findAll(Map<String, List<String>> filterParams) {
    List<LogicalMeterEntity> all = logicalMeterJpaRepository.findAll(
      filterMapper.map(filterParams)
    );

    return mapAndCollect(all);
  }

  @Override
  public Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    org.springframework.data.domain.Page<LogicalMeterEntity> all =
      logicalMeterJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          sortingMapper.getAsSpringSort(pageable.getSort())
        )
      );

    return new PageAdapter<>(
      all.map(
        logicalMeter -> logicalMeterMapper.toDomainModel(
          logicalMeter,
          getLongListMap(all.getContent())
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

  private List<PhysicalMeterStatusLogEntity> getStatusesForMeters(
    List<LogicalMeterEntity> logicalMeterEntities
  ) {
    List<Long> physicalMeterIds = new ArrayList<>();
    logicalMeterEntities.forEach(
      logicalMeterEntity -> logicalMeterEntity.physicalMeters.forEach(
        physicalMeterEntity -> physicalMeterIds.add(physicalMeterEntity.id)
      )
    );

    return physicalMeterStatusLogJpaRepository.findAllByPhysicalMeterIdIn(physicalMeterIds);
  }

  private List<LogicalMeter> mapAndCollect(List<LogicalMeterEntity> all) {
    Map<Long, List<PhysicalMeterStatusLogEntity>> mappedStatuses = getLongListMap(all);

    return all
      .stream()
      .map(logicalMeterEntity -> logicalMeterMapper.toDomainModel(
        logicalMeterEntity,
        mappedStatuses
        )
      ).collect(toList());
  }

  private Map<Long, List<PhysicalMeterStatusLogEntity>> getLongListMap(
    List<LogicalMeterEntity> all
  ) {
    List<PhysicalMeterStatusLogEntity> statusesForMeters = getStatusesForMeters(all);

    Map<Long, List<PhysicalMeterStatusLogEntity>> mappedStatuses = new HashMap<>();

    long meterId;
    PhysicalMeterStatusLogEntity logEntity;

    for (int x = 0; x < statusesForMeters.size(); x++) {
      logEntity = statusesForMeters.get(x);
      meterId = logEntity.physicalMeterId;

      if (!mappedStatuses.containsKey(meterId)) {
        mappedStatuses.put(meterId, new ArrayList<>());
      }

      mappedStatuses.get(meterId).add(logEntity);
    }
    return mappedStatuses;
  }
}
