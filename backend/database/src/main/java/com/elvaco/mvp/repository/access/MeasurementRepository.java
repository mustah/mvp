package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.usecase.Measurements;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.spring.PageAdapter;
import org.springframework.data.domain.PageRequest;

public class MeasurementRepository implements Measurements {
  private final MeasurementJpaRepository measurementJpaRepository;
  private final MeasurementFilterToPredicateMapper filterMapper;
  private final MeasurementMapper measurementMapper;

  public MeasurementRepository(MeasurementJpaRepository measurementJpaRepository,
                        MeasurementFilterToPredicateMapper filterMapper,
                        MeasurementMapper measurementMapper) {
    this.measurementJpaRepository = measurementJpaRepository;
    this.filterMapper = filterMapper;
    this.measurementMapper = measurementMapper;
  }

  @Override
  public Page<Measurement> findAllScaled(String scale, Map<String, List<String>> filterParams,
                                         Pageable pageable) {
    return new PageAdapter<>(measurementJpaRepository.findAllScaled(
      scale,
      filterMapper.map(filterParams),
      new PageRequest(pageable.getPageNumber(), pageable.getPageSize()))
      .map(measurementMapper::toDomainModel));
  }

  @Override
  public Page<Measurement> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(measurementJpaRepository.findAll(filterMapper.map(filterParams),
      new PageRequest(pageable.getPageNumber(), pageable.getPageSize()))
      .map(measurementMapper::toDomainModel));
  }

  @Override
  public Measurement findOne(Long id) {
    return measurementMapper.toDomainModel(measurementJpaRepository.findOne(id));
  }
}
