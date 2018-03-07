package com.elvaco.mvp.database.repository.access;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;
import com.elvaco.mvp.database.repository.queryfilters.QueryFilters;

import static java.util.stream.Collectors.toList;

public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepositoryImpl measurementJpaRepository;
  private final QueryFilters queryFilters;
  private final MeasurementMapper measurementMapper;

  public MeasurementRepository(
    MeasurementJpaRepositoryImpl measurementJpaRepository,
    QueryFilters queryFilters,
    MeasurementMapper measurementMapper
  ) {
    this.measurementJpaRepository = measurementJpaRepository;
    this.queryFilters = queryFilters;
    this.measurementMapper = measurementMapper;
  }

  @Override
  public List<Measurement> findAllByScale(String scale, RequestParameters parameters) {
    return measurementJpaRepository.findAllScaled(scale, queryFilters.toExpression(parameters))
      .stream()
      .map(measurementMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<Measurement> findAll(RequestParameters parameters) {
    return measurementJpaRepository.findAll(queryFilters.toExpression(parameters))
      .stream()
      .map(measurementMapper::toDomainModel)
      .collect(toList());
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
