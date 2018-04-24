package com.elvaco.mvp.database.repository.access;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeasurementMapper;
import com.elvaco.mvp.database.util.SqlErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepository measurementJpaRepository;

  @Override
  public Optional<Measurement> findById(Long id) {
    return Optional.ofNullable(measurementJpaRepository.findOne(id))
      .map(MeasurementMapper::toDomainModel);
  }

  @Override
  public Measurement save(Measurement measurement) {
    try {
      return MeasurementMapper.toDomainModel(
        measurementJpaRepository.save(
          MeasurementMapper.toEntity(measurement)
        )
      );
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public Collection<Measurement> save(Collection<Measurement> measurements) {
    List<MeasurementEntity> measurementEntities = measurements.stream()
      .map(MeasurementMapper::toEntity)
      .collect(toList());
    try {
      return measurementJpaRepository.save(measurementEntities)
        .stream()
        .map(MeasurementMapper::toDomainModel)
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public List<MeasurementValue> findAverageForPeriod(
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
        projection.getDoubleValue(),
        projection.getInstant()
      ))
      .collect(toList());
  }

  @Override
  public List<MeasurementValue> findSeriesForPeriod(
    UUID meterId,
    String quantity,
    String unit,
    SeriesDisplayMode mode,
    ZonedDateTime from,
    ZonedDateTime to
  ) {
    try {
      return measurementJpaRepository.getSeriesForPeriod(
        meterId,
        quantity,
        unit,
        mode.toString(),
        OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
        OffsetDateTime.ofInstant(to.toInstant(), from.getZone())
      ).stream()
        .map(projection -> new MeasurementValue(
          projection.getDoubleValue(),
          projection.getInstant()
        ))
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex, unit);
    }
  }

  @Override
  public Optional<Measurement> findByPhysicalMeterIdAndQuantityAndCreated(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  ) {
    return measurementJpaRepository.findByPhysicalMeterIdAndQuantityAndCreated(
      physicalMeterId, quantity, created
    ).map(MeasurementMapper::toDomainModel);
  }

  @Override
  public List<Measurement> findLatestValues(UUID physicalMeterId) {
    return measurementJpaRepository.findLatestForPhysicalMeter(physicalMeterId)
      .stream()
      .map(MeasurementMapper::toDomainModel)
      .collect(toList());
  }
}
