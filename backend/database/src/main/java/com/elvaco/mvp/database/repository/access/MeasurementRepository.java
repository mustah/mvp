package com.elvaco.mvp.database.repository.access;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
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
      .map(MeasurementEntityMapper::toDomainModel);
  }

  @Override
  public Measurement save(Measurement measurement) {
    try {
      return MeasurementEntityMapper.toDomainModel(
        measurementJpaRepository.save(MeasurementEntityMapper.toEntity(measurement))
      );
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public Collection<Measurement> save(Collection<Measurement> measurements) {
    List<MeasurementEntity> measurementEntities = measurements.stream()
      .map(MeasurementEntityMapper::toEntity)
      .collect(toList());
    try {
      return measurementJpaRepository.save(measurementEntities).stream()
        .map(MeasurementEntityMapper::toDomainModel)
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public List<MeasurementValue> findAverageForPeriod(
    List<UUID> meterIds,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurementJpaRepository.getAverageForPeriod(
      meterIds,
      resolution.toString(),
      seriesQuantity.name,
      seriesQuantity.presentationUnit(),
      seriesQuantity.seriesDisplayMode().toString(),
      OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
      OffsetDateTime.ofInstant(to.toInstant(), from.getZone())
    ).stream()
      .map(this::projectionToMeasurementValue)
      .collect(toList());
  }

  @Override
  public Optional<Measurement> findBy(
    UUID physicalMeterId,
    ZonedDateTime created,
    String quantity
  ) {
    return measurementJpaRepository.findBy(physicalMeterId, quantity, created)
      .map(MeasurementEntityMapper::toDomainModel);
  }

  @Override
  public Optional<Measurement> findLatestReadout(
    UUID meterId,
    ZonedDateTime before,
    Quantity quantity
  ) {
    return measurementJpaRepository.findLatestReadout(
      meterId,
      before.toOffsetDateTime(),
      quantity.name,
      quantity.presentationUnit()
    ).map(MeasurementEntityMapper::toDomainModel);
  }

  @Override
  public List<MeasurementValue> findSeriesForPeriod(
    UUID meterId,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    try {
      return measurementJpaRepository.getSeriesForPeriod(
        meterId,
        seriesQuantity.name,
        seriesQuantity.presentationUnit(),
        seriesQuantity.seriesDisplayMode().toString(),
        OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
        OffsetDateTime.ofInstant(to.toInstant(), from.getZone()),
        resolution.toString()
      ).stream()
        .map(this::projectionToMeasurementValue)
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex, seriesQuantity.presentationUnit());
    }
  }

  private MeasurementValue projectionToMeasurementValue(MeasurementValueProjection projection) {
    return new MeasurementValue(projection.getDoubleValue(), projection.getInstant());
  }
}
