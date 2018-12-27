package com.elvaco.mvp.database.repository.access;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.database.util.SqlErrorMapper;

import org.springframework.dao.DataIntegrityViolationException;

import static java.util.stream.Collectors.toList;

public class MeasurementRepository implements Measurements {

  private final MeasurementJpaRepository measurementJpaRepository;
  private final QuantityProvider quantityProvider;
  private final UnitConverter unitConverter;
  private final MeasurementEntityMapper measurementEntityMapper;

  public MeasurementRepository(
    MeasurementJpaRepository measurementJpaRepository,
    QuantityProvider quantityProvider,
    UnitConverter unitConverter,
    QuantityEntityMapper quantityEntityMapper
  ) {
    this.measurementJpaRepository = measurementJpaRepository;
    this.quantityProvider = quantityProvider;
    this.unitConverter = unitConverter;
    this.measurementEntityMapper = new MeasurementEntityMapper(
      unitConverter,
      quantityProvider,
      quantityEntityMapper
    );
  }

  @Override
  public Measurement save(Measurement measurement) {
    try {
      return measurementEntityMapper.toDomainModel(
        measurementJpaRepository.save(measurementEntityMapper.toEntity(measurement))
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
        quantityProvider.getByName(quantity).getId(),
        measurementUnit.getValue()
      );
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public List<MeasurementValue> findAverageForPeriod(MeasurementParameter parameter) {
    List<MeasurementValueProjection> averageForPeriod;
    if (parameter.getQuantity().seriesDisplayMode() == SeriesDisplayMode.CONSUMPTION) {
      averageForPeriod = measurementJpaRepository.getAverageForPeriodConsumption(
        parameter.getPhysicalMeterIds(),
        parameter.getResolution().asInterval(),
        parameter.getQuantity().name,
        parameter.getResolution().getStart(parameter.getFrom()),
        parameter.getResolution().getStart(parameter.getTo())
      );
    } else {
      averageForPeriod = measurementJpaRepository.getAverageForPeriod(
        parameter.getPhysicalMeterIds(),
        parameter.getResolution().asInterval(),
        parameter.getQuantity().name,
        parameter.getResolution().getStart(parameter.getFrom()),
        parameter.getResolution().getStart(parameter.getTo())
      );
    }

    return averageForPeriod.stream()
      .map(projection -> toMeasurementValue(projection, parameter.getQuantity()))
      .collect(toList());
  }

  @Override
  public List<MeasurementValue> findSeriesForPeriod(MeasurementParameter parameter) {
    try {
      List<MeasurementValueProjection> seriesForPeriod;
      if (parameter.getQuantity().seriesDisplayMode() == SeriesDisplayMode.CONSUMPTION) {
        seriesForPeriod = measurementJpaRepository.getSeriesForPeriodConsumption(
          parameter.getPhysicalMeterIds().get(0),
          parameter.getQuantity().name,
          parameter.getResolution().getStart(parameter.getFrom()),
          parameter.getResolution().getStart(parameter.getTo()),
          parameter.getResolution().asInterval()
        );
      } else {
        seriesForPeriod = measurementJpaRepository.getSeriesForPeriod(
          parameter.getPhysicalMeterIds().get(0),
          parameter.getQuantity().name,
          parameter.getResolution().getStart(parameter.getFrom()),
          parameter.getResolution().getStart(parameter.getTo()),
          parameter.getResolution().asInterval()
        );
      }
      return seriesForPeriod.stream()
        .map((projection) -> toMeasurementValue(projection, parameter.getQuantity()))
        .collect(toList());
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(
        ex,
        parameter.getQuantity().presentationUnit()
      );
    }
  }

  @Override
  public List<Measurement> findAll(RequestParameters parameters) {
    return measurementJpaRepository.findAll(parameters).stream()
      .map(measurementEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Measurement> firstForPhysicalMeterWithinDateRange(
    UUID physicalMeterId, ZonedDateTime after, ZonedDateTime beforeOrEquals
  ) {
    return measurementJpaRepository.firstForPhysicalMeter(physicalMeterId, after, beforeOrEquals)
      .map(measurementEntityMapper::toDomainModel);
  }

  private MeasurementValue toMeasurementValue(
    MeasurementValueProjection projection,
    Quantity quantity
  ) {
    Double value = Optional.ofNullable(projection.getValue())
      .map(unitValue ->
        new MeasurementUnit(quantityProvider.getByName(quantity.name).storageUnit, unitValue))
      .map(measurementUnit -> unitConverter.convert(measurementUnit, quantity.presentationUnit()))
      .map(MeasurementUnit::getValue)
      .orElse(null);
    return new MeasurementValue(value, projection.getInstant());
  }
}
