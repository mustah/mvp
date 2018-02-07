package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;

import org.springframework.data.domain.PageRequest;

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
  public LogicalMeter findById(Long id) {
    return logicalMeterMapper.toDomainModel(
      logicalMeterJpaRepository.findOne(id)
    );
  }

  @Override
  public List<LogicalMeter> findAll() {
    return logicalMeterJpaRepository.findAll()
      .stream()
      .map(logicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(
      logicalMeterJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
      ).map(logicalMeterMapper::toDomainModel)
    );
  }

  @Override
  public void save(LogicalMeter logicalMeter) {
    logicalMeterJpaRepository.save(
      logicalMeterMapper.toEntity(logicalMeter)
    );
  }

  @Override
  public void deleteAll() {
    logicalMeterJpaRepository.deleteAll();
  }
}
