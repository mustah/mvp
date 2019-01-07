package com.elvaco.mvp.database.repository.access;

import java.time.Instant;
import java.time.OffsetDateTime;
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
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.database.util.SqlErrorMapper;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static java.util.stream.Collectors.toList;

public class MeasurementRepository implements Measurements {

  private final DSLContext dsl;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final QuantityProvider quantityProvider;
  private final UnitConverter unitConverter;
  private final MeasurementEntityMapper measurementEntityMapper;

  public MeasurementRepository(
    DSLContext dsl,
    MeasurementJpaRepository measurementJpaRepository,
    QuantityProvider quantityProvider,
    UnitConverter unitConverter,
    QuantityEntityMapper quantityEntityMapper
  ) {
    this.dsl = dsl;
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
    if (parameter.getQuantity().isConsumption()) {
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
      .map(projection -> toMeasurementValue(
        projection.getInstant(),
        projection.getValue(),
        parameter.getQuantity()
      ))
      .collect(toList());
  }

  @Override
  public List<MeasurementValue> findSeriesForPeriod(MeasurementParameter parameter) {
    try {
      ResultQuery<?> q;
      if (parameter.getQuantity().isConsumption()) {
        q = getConsumptionSeriesQuery(parameter);
      } else {
        q = getReadoutSeriesQuery(parameter);
      }
      return q.fetchInto(MeasurementValue.class).stream()
        .map(value -> toMeasurementValue(value.when, value.value, parameter.getQuantity()))
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

  Condition getSeriesJoinCondition(
    MeasurementParameter parameter,
    Field<OffsetDateTime> dateTimeField
  ) {
    return MEASUREMENT.PHYSICAL_METER_ID.equal(parameter.getPhysicalMeterIds()
      .get(0))
      .and(MEASUREMENT.CREATED.equal(dateTimeField))
      .and(MEASUREMENT.QUANTITY.equal(
        dsl.select(QUANTITY.ID)
          .from(QUANTITY)
          .where(QUANTITY.NAME.equal(parameter.getQuantity().name))
      ));
  }

  private ResultQuery<Record2<Double, OffsetDateTime>> getConsumptionSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);

    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);
    Condition condition = getSeriesJoinCondition(parameter, dateTimeField);
    var measurementSeries = dsl.select(
      DSL.lead(MEASUREMENT.VALUE)
        .over(DSL.orderBy(MEASUREMENT.CREATED.asc()))
        .minus(MEASUREMENT.VALUE)
        .as("value"),
      dateTimeField.as("when")
    ).from(dateSerieTable)
      .leftJoin(MEASUREMENT).on(condition).asTable("measurement_serie");

    Field<OffsetDateTime> when = measurementSeries.field("when", OffsetDateTime.class);
    return dsl.select(
      measurementSeries.field("value", Double.class),
      when
    ).from(measurementSeries)
      .where(
        when.greaterOrEqual(parameter.getResolution().getStart(parameter.getFrom())),
        when.lessOrEqual(parameter.getResolution().getStart(parameter.getTo()))
      )
      .orderBy(when.asc());
  }

  private ResultQuery<Record2<Double, OffsetDateTime>> getReadoutSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);

    Condition condition = getSeriesJoinCondition(parameter,
      DSL.field(dateSerie, OffsetDateTime.class)
    );
    return dsl.select(MEASUREMENT.VALUE, DSL.field(dateSerie, OffsetDateTime.class))
      .from(dateSerieTable)
      .leftJoin(MEASUREMENT)
      .on(condition);
  }

  private Table<Record> dateSerieFor(MeasurementParameter parameter) {
    String expr;
    if (parameter.getQuantity().isConsumption()) {
      expr = "generate_series({0}, {1} + cast({2} as interval), {2}::interval)";
    } else {
      expr = "generate_series({0}, {1}, {2}::interval)";
    }
    return DSL.table(
      expr,
      parameter.getResolution().getStart(parameter.getFrom()),
      parameter.getResolution().getStart(parameter.getTo()),
      parameter.getResolution().asInterval()
    );
  }

  private MeasurementValue toMeasurementValue(
    Instant when,
    Double fromValue,
    Quantity quantity
  ) {
    Double value = Optional.ofNullable(fromValue)
      .map(unitValue ->
        new MeasurementUnit(quantityProvider.getByName(quantity.name).storageUnit, unitValue))
      .map(measurementUnit -> unitConverter.convert(measurementUnit, quantity.presentationUnit()))
      .map(MeasurementUnit::getValue)
      .orElse(null);
    return new MeasurementValue(value, when);
  }
}
