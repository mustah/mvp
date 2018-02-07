package com.elvaco.mvp.database.repository.access;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;

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
  public List<Measurement> findAllByScale(String scale, Map<String, List<String>> filterParams) {
    return measurementJpaRepository.findAllScaled(
      scale,
      filterMapper.map(filterParams)
    ).stream().map(measurementMapper::toDomainModel).collect(toList());
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

  @Override
  public Measurement save(Measurement measurement) {
    return measurementMapper.toDomainModel(
      measurementJpaRepository.save(
        measurementMapper.toEntity(measurement)
      )
    );
  }

  @Override
  public Collection<Measurement> save(Collection<Measurement> measurements) {
    List<MeasurementEntity> measurementEntities = measurements.stream()
      .map(measurementMapper::toEntity)
      .collect(toList());
    return
      measurementJpaRepository.save(measurementEntities)
        .stream()
        .map(measurementMapper::toDomainModel)
        .collect(toList());
  }
}
