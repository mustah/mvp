package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Order;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterSortingMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.stream.Collectors.toList;

public class LogicalMeterRepository implements LogicalMeters {

  private final LogicalMeterJpaRepository logicalMeterJpaRepository;
  private final LogicalMeterMapper logicalMeterMapper;
  private final LogicalMeterToPredicateMapper filterMapper;

  public LogicalMeterRepository(
    LogicalMeterJpaRepository logicalMeterJpaRepository,
    LogicalMeterToPredicateMapper filterMapper,
    LogicalMeterMapper logicalMeterMapper
  ) {
    this.logicalMeterJpaRepository = logicalMeterJpaRepository;
    this.filterMapper = filterMapper;
    this.logicalMeterMapper = logicalMeterMapper;
  }

  @Override
  public Optional<LogicalMeter> findById(Long id) {
    return logicalMeterJpaRepository.findById(id).map(logicalMeterMapper::toDomainModel);
  }

  @Override
  public List<LogicalMeter> findAll() {
    return logicalMeterJpaRepository.findAll()
      .stream()
      .map(logicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAll(Map<String, List<String>> filterParams) {
    return logicalMeterJpaRepository.findAll(filterMapper.map(filterParams))
      .stream()
      .map(logicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          getAsSpringSort(pageable.getSort())
        )
      ).map(logicalMeterMapper::toDomainModel)
    );
  }

  public org.springframework.data.domain.Sort getAsSpringSort(com.elvaco.mvp.core.spi.data.Sort sort) {
    if (sort.getOrders().size() > 0) {
      return new org.springframework.data.domain.Sort(
        sort.getOrders().stream().map(this::mapOrderToSprintOrder).collect(toList())
      );
    } else {
      return null;
    }
  }


  private Sort.Order mapOrderToSprintOrder(Order order) {

    LogicalMeterSortingMapper logicalMeterSortingMapper = new LogicalMeterSortingMapper();

    org.springframework.data.domain.Sort.Order springOrder =
      new org.springframework.data.domain.Sort.Order(
        Direction.fromString(order.getDirection().name()),
        logicalMeterSortingMapper.getSortingMap().get(order.getProperty())
      );

    if (order.isIgnoreCase()) {
      return springOrder.ignoreCase();
    } else {
      return springOrder;
    }
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
    Long organisationId, String externalId
  ) {
    return logicalMeterJpaRepository
      .findByOrganisationIdAndExternalId(organisationId, externalId)
      .map(logicalMeterMapper::toDomainModel);
  }
}
