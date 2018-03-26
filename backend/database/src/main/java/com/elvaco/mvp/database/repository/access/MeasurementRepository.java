package com.elvaco.mvp.database.repository.access;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;
import com.elvaco.mvp.database.repository.queryfilters.QueryFilters;

import static java.util.stream.Collectors.toList;

public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepository measurementJpaRepository;
  private final QueryFilters queryFilters;
  private final MeasurementMapper measurementMapper;

  public MeasurementRepository(
    MeasurementJpaRepository measurementJpaRepository,
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
  public Optional<Measurement> findByPhysicalMeterIdAndCreated(
    UUID physicalMeterId,
    ZonedDateTime created
  ) {
    return measurementJpaRepository.findByPhysicalMeterIdAndCreated(
      physicalMeterId, created
    ).map(measurementMapper::toDomainModel);
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

  @Override
  public List<MeasurementValue> getAverageForPeriod(
    List<UUID> meterIds,
    String quantity,
    String unit,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {

    return measurementJpaRepository.getAverageForPeriod(
      meterIds,
      resolution.toString(),
      quantity,
      unit,
      OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
      OffsetDateTime.ofInstant(to.toInstant(), from.getZone())
    ).stream()
      .map(projection -> new MeasurementValue(
        projection.getValueValue(),
        projection.getInstant()
      ))
      .collect(toList());
  }

}
