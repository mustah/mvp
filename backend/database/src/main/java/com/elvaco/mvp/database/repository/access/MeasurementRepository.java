package com.elvaco.mvp.database.repository.access;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementSortingMapper;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.MeasurementQueryFilters;
import com.elvaco.mvp.database.util.SqlErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepository measurementJpaRepository;
  private final MeasurementSortingMapper sortingMapper;

  @Override
  public Optional<Measurement> findById(Measurement.Id id) {
    return Optional.ofNullable(measurementJpaRepository.findOne(toPk(id)))
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
  public void createOrUpdate(
    PhysicalMeter physicalMeter,
    ZonedDateTime created,
    String quantity,
    String unit,
    double value
  ) {
    try {
      MeasurementUnit measurementUnit = new MeasurementUnit(unit, value);
      measurementJpaRepository.createOrUpdate(
        physicalMeter.id,
        created,
        QuantityAccess.singleton().getByName(quantity).getId(),
        measurementUnit.getUnit(),
        measurementUnit.getValue()
      );
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
    List<MeasurementValueProjection> averageForPeriod;

    switch (seriesQuantity.seriesDisplayMode()) {
      case CONSUMPTION:
        averageForPeriod = measurementJpaRepository.getAverageForPeriodConsumption(
          meterIds,
          resolution.toString(),
          seriesQuantity.name,
          seriesQuantity.presentationUnit(),
          OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
          OffsetDateTime.ofInstant(to.toInstant(), from.getZone()),
          maxNumberOfDataPointsForResolution(resolution)
        );
        break;
      default:
        averageForPeriod = measurementJpaRepository.getAverageForPeriod(
          meterIds,
          resolution.toString(),
          seriesQuantity.name,
          seriesQuantity.presentationUnit(),
          OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
          OffsetDateTime.ofInstant(to.toInstant(), from.getZone())
        );
        break;
    }

    return averageForPeriod.stream()
      .map(this::projectionToMeasurementValue)
      .collect(toList());
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
      List<MeasurementValueProjection> seriesForPeriod;

      switch (seriesQuantity.seriesDisplayMode()) {
        case CONSUMPTION:
          seriesForPeriod = measurementJpaRepository.getSeriesForPeriodConsumption(
            meterId,
            seriesQuantity.name,
            seriesQuantity.presentationUnit(),
            OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
            OffsetDateTime.ofInstant(to.toInstant(), from.getZone()),
            resolution.toString(),
            maxNumberOfDataPointsForResolution(resolution)
          );
          break;
        default:
          seriesForPeriod = measurementJpaRepository.getSeriesForPeriod(
            meterId,
            seriesQuantity.name,
            seriesQuantity.presentationUnit(),
            OffsetDateTime.ofInstant(from.toInstant(), from.getZone()),
            OffsetDateTime.ofInstant(to.toInstant(), from.getZone()),
            resolution.toString()
          );
      }

      return seriesForPeriod.stream()
        .map(this::projectionToMeasurementValue)
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex, seriesQuantity.presentationUnit());
    }
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
  public Page<Measurement> findAll(
    RequestParameters parameters, Pageable pageable
  ) {
    org.springframework.data.domain.Page<MeasurementEntity> all = measurementJpaRepository.findAll(
      new MeasurementQueryFilters().toExpression(parameters),
      new PageRequest(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortingMapper.getAsSpringSort(pageable.getSort())
      )
    );
    return new PageAdapter<>(all.map(MeasurementEntityMapper::toDomainModel));
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

  private int maxNumberOfDataPointsForResolution(TemporalResolution resolution) {
    switch (resolution) {
      case month:
        return 3;
      case hour:
        return 96;
      case day:
      default:
        return 10;
    }
  }

  private MeasurementValue projectionToMeasurementValue(MeasurementValueProjection projection) {
    return new MeasurementValue(projection.getDoubleValue(), projection.getInstant());
  }

  private static MeasurementPk toPk(Measurement.Id id) {
    return new MeasurementPk(
      id.created,
      QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(id.quantity)),
      PhysicalMeterEntityMapper.toEntity(id.physicalMeter)
    );
  }
}
