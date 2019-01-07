package com.elvaco.mvp.database.repository.access;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
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
import org.jooq.Record3;
import org.jooq.ResultQuery;
import org.jooq.Row1;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
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
  public Map<UUID, List<MeasurementValue>> findSeriesForPeriod(MeasurementParameter parameter) {
    try {
      ResultQuery<Record3<UUID, Double, OffsetDateTime>> q;
      if (parameter.getQuantity().isConsumption()) {
        q = getConsumptionSeriesQuery(parameter);
      } else {
        q = getReadoutSeriesQuery(parameter);
      }

      return q.fetch().stream()
        .collect(groupingBy(
          k -> k.get(k.field1()),
          mapping(
            t -> toMeasurementValue(
              t.get(t.field3()).toInstant(),
              t.get(t.field2()),
              parameter.getQuantity()
            ), toList())
          )
        );
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

  private Condition getSeriesJoinCondition(
    MeasurementParameter parameter,
    Field<UUID> meterIdField,
    Field<OffsetDateTime> dateTimeField
  ) {
    return MEASUREMENT.PHYSICAL_METER_ID.equal(meterIdField)
      .and(MEASUREMENT.CREATED.equal(dateTimeField))
      .and(MEASUREMENT.QUANTITY.equal(
        dsl.select(QUANTITY.ID)
          .from(QUANTITY)
          .where(QUANTITY.NAME.equal(parameter.getQuantity().name))));
  }

  private ResultQuery<Record3<UUID, Double, OffsetDateTime>> getConsumptionSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);

    Field<UUID> meterIdField = DSL.field("meters.id", UUID.class);
    Field<UUID> meterId = DSL.field("meter_id", UUID.class);
    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);

    Row1<UUID>[] meterIdRows = getMeterIdRows(parameter);
    Condition condition = getSeriesJoinCondition(parameter, meterIdField, dateTimeField);
    var measurementSeries = dsl.select(
      meterIdField.as(meterId),
      DSL.lead(MEASUREMENT.VALUE)
        .over(DSL.orderBy(meterIdField.asc(), MEASUREMENT.CREATED.asc()))
        .minus(MEASUREMENT.VALUE)
        .as("value"),
      dateTimeField.as("when")
    ).from(DSL.values(meterIdRows).as("meters", "id"))
      .crossJoin(dateSerieTable)
      .leftJoin(MEASUREMENT).on(condition).asTable("measurement_serie");

    Field<OffsetDateTime> when = measurementSeries.field("when", OffsetDateTime.class);
    return dsl.select(
      meterId,
      measurementSeries.field("value", Double.class),
      when
    ).from(measurementSeries)
      .where(
        when.greaterOrEqual(parameter.getResolution().getStart(parameter.getFrom())),
        when.lessOrEqual(parameter.getResolution().getStart(parameter.getTo()))
      )
      .orderBy(when.asc());
  }

  private ResultQuery<Record3<UUID, Double, OffsetDateTime>> getReadoutSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);

    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);
    Field<UUID> meterIdField = DSL.field("meters.id", UUID.class);

    Condition condition = getSeriesJoinCondition(parameter, meterIdField, dateTimeField);

    Row1<UUID>[] meterIdRows = getMeterIdRows(parameter);
    return dsl.select(
      meterIdField,
      MEASUREMENT.VALUE,
      dateTimeField
    ).from(DSL.values(meterIdRows).as("meters", "id"))
      .crossJoin(dateSerieTable)
      .leftJoin(MEASUREMENT)
      .on(condition);
  }

  @SuppressWarnings("unchecked")
  private Row1<UUID>[] getMeterIdRows(MeasurementParameter parameter) {
    return (Row1<UUID>[]) parameter.getPhysicalMeterIds()
      .stream()
      .map(DSL::row)
      .toArray(Row1[]::new);
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
