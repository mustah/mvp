package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.usecase.Measurements;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.spring.PageAdapter;
import org.springframework.data.domain.PageRequest;

import static java.util.stream.Collectors.toList;

public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepository measurementJpaRepository;
  private final MeasurementFilterToPredicateMapper filterMapper;
  private final MeasurementMapper measurementMapper;

  public MeasurementRepository(
    MeasurementJpaRepository measurementJpaRepository,
    MeasurementFilterToPredicateMapper filterMapper,
    MeasurementMapper measurementMapper
  ) {
    this.measurementJpaRepository = measurementJpaRepository;
    this.filterMapper = filterMapper;
    this.measurementMapper = measurementMapper;
  }

  @Override
  public Page<Measurement> findAllByScale(
    String scale, Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return new PageAdapter<>(
      measurementJpaRepository.findAllScaled(
        scale,
        filterMapper.map(filterParams),
        new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
      ).map(measurementMapper::toDomainModel)
    );
  }

  @Override
  public List<Measurement> findAllByScale(String scale, Map<String, List<String>> filterParams) {
    return measurementJpaRepository.findAllScaled(
      scale,
      filterMapper.map(filterParams)
    ).stream().map(measurementMapper::toDomainModel).collect(toList());
  }

  @Override
  public Page<Measurement> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(
      measurementJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
      ).map(measurementMapper::toDomainModel)
    );
  }

  @Override
  public List<Measurement> findAll(Map<String, List<String>> filterParams) {
    return
      measurementJpaRepository.findAll(
        filterMapper.map(filterParams)
      ).stream().map(measurementMapper::toDomainModel).collect(toList());
  }

  @Override
  public Optional<Measurement> findById(Long id) {
    return Optional.ofNullable(measurementJpaRepository.findOne(id))
      .map(measurementMapper::toDomainModel);
  }
}
