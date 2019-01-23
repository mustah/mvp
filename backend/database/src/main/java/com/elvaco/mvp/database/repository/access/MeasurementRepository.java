package com.elvaco.mvp.database.repository.access;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.database.util.SqlErrorMapper;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION_QUANTITIES;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.lead;
import static org.jooq.impl.DSL.trueCondition;

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
  public void createOrUpdate(Measurement measurement) {
    try {
      MeasurementUnit measurementUnit = new MeasurementUnit(measurement.unit, measurement.value);
      Quantity quantity = quantityProvider.getByName(measurement.quantity);
      measurementJpaRepository.createOrUpdate(
        measurement.physicalMeter.id,
        measurement.created,
        quantity.id,
        unitConverter.convert(measurementUnit, quantity.storageUnit).getValue()
      );
    } catch (DataIntegrityViolationException ex) {
      throw SqlErrorMapper.mapDataIntegrityViolation(ex);
    }
  }

  @Override
  public Map<String, List<MeasurementValue>> findAverageForPeriod(
    MeasurementParameter parameter
  ) {
    Map<String, List<MeasurementValue>> result = new HashMap<>();

    var consumptionParameter = getConsumptionParameter(parameter);
    if (!consumptionParameter.getQuantities().isEmpty()) {
      var r = getConsumptionAverageQuery(consumptionParameter);
      result.putAll(mapAverageForPeriod(consumptionParameter, r));
    }

    var readoutParameter = getReadoutParameter(parameter);
    if (!readoutParameter.getQuantities().isEmpty()) {
      var r = getReadoutAverageQuery(readoutParameter);
      result.putAll(mapAverageForPeriod(readoutParameter, r));
    }

    return result;
  }

  @Override
  public Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(
    MeasurementParameter parameter
  ) {
    Map<MeasurementKey, List<MeasurementValue>> result = new HashMap<>();

    MeasurementParameter readoutParameter = getReadoutParameter(parameter);
    if (!readoutParameter.getQuantities().isEmpty()) {
      var r = getReadoutSeriesQuery(readoutParameter);
      result.putAll(mapSeriesForPeriod(readoutParameter, r));
    }

    MeasurementParameter consumptionParameter = getConsumptionParameter(parameter);
    if (!consumptionParameter.getQuantities().isEmpty()) {
      var r = getConsumptionSeriesQuery(consumptionParameter);
      result.putAll(mapSeriesForPeriod(consumptionParameter, r));
    }

    return result;
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

  private MeasurementParameter getReadoutParameter(MeasurementParameter parameter) {
    return parameter.toBuilder()
      .quantities(parameter.getQuantities()
        .stream()
        .filter(q -> !q.isConsumption())
        .collect(toList()))
      .build();
  }

  private MeasurementParameter getConsumptionParameter(MeasurementParameter parameter) {
    return parameter.toBuilder()
      .quantities(parameter.getQuantities()
        .stream()
        .filter(q -> q.isConsumption())
        .collect(toList()))
      .build();
  }

  private Map<MeasurementKey, List<MeasurementValue>> mapSeriesForPeriod(
    MeasurementParameter parameter,
    ResultQuery<Record4<UUID, String, Double, OffsetDateTime>> r
  ) {

    Map<String, Quantity> quantityMap = getQuantityMap(parameter);

    return r.fetch().stream()
      .collect(groupingBy(
        k -> new MeasurementKey(k.get(k.field1()), k.get(k.field2())),
        mapping(
          t -> toMeasurementValueConvertedToUnitFromQuantity(
            t.get(t.field4()).toInstant(),
            t.get(t.field3()),
            quantityMap.get(t.get(t.field2()))
          ), toList())
        )
      );
  }

  private Map<String, List<MeasurementValue>> mapAverageForPeriod(
    MeasurementParameter parameter,
    ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> r
  ) {

    Map<String, Quantity> quantityMap = getQuantityMap(parameter);

    return r.fetch().stream()
      .collect(groupingBy(
        k -> k.get(k.field2()),
        mapping(
          t -> toMeasurementValueConvertedToUnitFromQuantity(
            t.get(t.field3()).toInstant(),
            t.get(t.field1()),
            quantityMap.get(t.get(t.field2()))
          ), toList())
        )
      );
  }

  private Map<String, Quantity> getQuantityMap(MeasurementParameter parameter) {
    return parameter.getQuantities()
      .stream()
      .collect(toMap(q -> q.name, q -> q));
  }

  private ResultQuery<Record4<UUID, String, Double, OffsetDateTime>> getConsumptionSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);

    Field<UUID> meterId = DSL.field("meter_id", UUID.class);
    Field<String> quantity = DSL.field("quantity", String.class);
    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);

    Condition measurementCondition = getMeasurementJoinCondition(dateTimeField);
    Condition quantityCondition = getQuantityCondition(parameter);
    Condition organisationCondition = getOrganisationJoinCondition();

    var measurementSeries = dsl.select(
      LOGICAL_METER.ID.as(meterId),
      QUANTITY.NAME.as(quantity),
      lead(MEASUREMENT.VALUE)
        .over(DSL.orderBy(LOGICAL_METER.ID.asc(), MEASUREMENT.CREATED.asc()))
        .minus(MEASUREMENT.VALUE)
        .as("value"),
      dateTimeField.as("when")
    ).from(LOGICAL_METER)
      .join(METER_DEFINITION_QUANTITIES)
      .on(LOGICAL_METER.METER_DEFINITION_TYPE.eq(METER_DEFINITION_QUANTITIES.METER_DEFINITION_TYPE))
      .innerJoin(QUANTITY)
      .on(QUANTITY.ID.eq(METER_DEFINITION_QUANTITIES.QUANTITY_ID).and(quantityCondition))
      .innerJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ID.eq(PHYSICAL_METER.LOGICAL_METER_ID).and(organisationCondition))
      .rightOuterJoin(dateSerieTable)
      .on(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, dateTimeField))
      .leftJoin(MEASUREMENT)
      .on(measurementCondition)
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));

    Field<OffsetDateTime> when = measurementSeries.field("when", OffsetDateTime.class);
    return dsl.select(
      meterId,
      measurementSeries.field("quantity", String.class),
      measurementSeries.field("value", Double.class),
      when
    ).from(measurementSeries)
      .where(
        when.greaterOrEqual(parameter.getResolution().getStart(parameter.getFrom())),
        when.lessOrEqual(parameter.getResolution().getStart(parameter.getTo()))
      )
      .orderBy(when.asc());
  }

  private ResultQuery<Record4<UUID, String, Double, OffsetDateTime>> getReadoutSeriesQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);
    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);

    Condition measurementCondition = getMeasurementJoinCondition(dateTimeField);
    Condition quantityCondition = getQuantityCondition(parameter);
    Condition organisationCondition = getOrganisationJoinCondition();

    return dsl.select(
      LOGICAL_METER.ID,
      QUANTITY.NAME,
      MEASUREMENT.VALUE,
      dateTimeField
    ).from(LOGICAL_METER)
      .join(METER_DEFINITION_QUANTITIES)
      .on(LOGICAL_METER.METER_DEFINITION_TYPE.eq(METER_DEFINITION_QUANTITIES.METER_DEFINITION_TYPE))
      .innerJoin(QUANTITY)
      .on(QUANTITY.ID.eq(METER_DEFINITION_QUANTITIES.QUANTITY_ID).and(quantityCondition))
      .innerJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ID.eq(PHYSICAL_METER.LOGICAL_METER_ID).and(organisationCondition))
      .rightOuterJoin(dateSerieTable)
      .on(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, dateTimeField))
      .leftJoin(MEASUREMENT)
      .on(measurementCondition)
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getReadoutAverageQuery(
    MeasurementParameter parameter
  ) {
    String dateSerie = "date_serie";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerie);
    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerie, OffsetDateTime.class);

    Condition measurementCondition = getMeasurementJoinCondition(dateTimeField);
    Condition quantityCondition = getQuantityCondition(parameter);
    Condition organisationCondition = getOrganisationJoinCondition();

    return dsl.select(
      avg(MEASUREMENT.VALUE),
      QUANTITY.NAME,
      dateTimeField
    ).from(LOGICAL_METER)
      .join(METER_DEFINITION_QUANTITIES)
      .on(LOGICAL_METER.METER_DEFINITION_TYPE.eq(METER_DEFINITION_QUANTITIES.METER_DEFINITION_TYPE))
      .innerJoin(QUANTITY)
      .on(QUANTITY.ID.eq(METER_DEFINITION_QUANTITIES.QUANTITY_ID).and(quantityCondition))
      .innerJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ID.eq(PHYSICAL_METER.LOGICAL_METER_ID).and(organisationCondition))
      .rightOuterJoin(dateSerieTable)
      .on(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, dateTimeField))
      .leftJoin(MEASUREMENT)
      .on(measurementCondition)
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()))
      .groupBy(dateTimeField, QUANTITY.NAME)
      .orderBy(dateTimeField, QUANTITY.NAME);
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getConsumptionAverageQuery(
    MeasurementParameter parameter
  ) {
    String dateSerieName = "date_serie";
    String consumptionName = "consumption";
    Table<Record> dateSerieTable = dateSerieFor(parameter).as(dateSerieName);

    Field<String> intervalStart = DSL.field("interval_start", String.class);
    Field<OffsetDateTime> dateTimeField = DSL.field(dateSerieName, OffsetDateTime.class);

    Condition measurementCondition = getMeasurementJoinCondition(dateTimeField);
    Condition quantityCondition = getQuantityCondition(parameter);
    Condition organisationCondition = getOrganisationJoinCondition();

    var series = dsl.select(
      lead(MEASUREMENT.VALUE).over()
        .partitionBy(LOGICAL_METER.ID, MEASUREMENT.QUANTITY).orderBy(MEASUREMENT.CREATED.asc())
        .minus(MEASUREMENT.VALUE).as(consumptionName),
      QUANTITY.NAME.as("quantity"),
      dateTimeField.as(intervalStart)
    ).from(LOGICAL_METER)
      .join(METER_DEFINITION_QUANTITIES)
      .on(LOGICAL_METER.METER_DEFINITION_TYPE.eq(METER_DEFINITION_QUANTITIES.METER_DEFINITION_TYPE))
      .innerJoin(QUANTITY)
      .on(QUANTITY.ID.eq(METER_DEFINITION_QUANTITIES.QUANTITY_ID).and(quantityCondition))
      .innerJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ID.eq(PHYSICAL_METER.LOGICAL_METER_ID).and(organisationCondition))
      .rightOuterJoin(dateSerieTable)
      .on(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, dateTimeField))
      .leftJoin(MEASUREMENT)
      .on(measurementCondition)
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));

    Field<OffsetDateTime> when = series.field("interval_start", OffsetDateTime.class);
    Field<BigDecimal> consumption = series.field(consumptionName, BigDecimal.class);
    Field<String> quantity = series.field("quantity", String.class);

    return dsl.select(
      avg(consumption),
      quantity,
      when
    ).from(series)
      .where(when.lessOrEqual(parameter.getResolution().getStart(parameter.getTo())))
      .groupBy(intervalStart, quantity)
      .orderBy(when.asc(), quantity);
  }

  private Condition getOrganisationJoinCondition() {
    return LOGICAL_METER.ORGANISATION_ID.eq(PHYSICAL_METER.ORGANISATION_ID);
  }

  private Condition getQuantityCondition(MeasurementParameter parameter) {
    if (parameter.getQuantities().isEmpty()) {
      return trueCondition();
    }
    return QUANTITY.NAME.in(parameter.getQuantities().stream().map(q -> q.name).collect(toList()));
  }

  private Condition getMeasurementJoinCondition(
    Field<OffsetDateTime> dateTimeField
  ) {
    return MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
      .and(MEASUREMENT.CREATED.equal(dateTimeField))
      .and(MEASUREMENT.QUANTITY.equal(QUANTITY.ID));
  }

  private Table<Record> dateSerieFor(MeasurementParameter parameter) {
    String expr;
    if (parameter.getQuantities().get(0).isConsumption()) {
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

  private MeasurementValue toMeasurementValueConvertedToUnitFromQuantity(
    Instant when,
    Number fromValue,
    Quantity quantity
  ) {
    Double value = Optional.ofNullable(fromValue)
      .map(unitValue ->
        new MeasurementUnit(
          quantityProvider.getByName(quantity.name).storageUnit,
          unitValue.doubleValue()
        ))
      .map(measurementUnit ->
        Optional.ofNullable(quantity.presentationUnit())
          .map(u -> unitConverter.convert(measurementUnit, quantity.presentationUnit()))
          .orElse(measurementUnit)
      )
      .map(MeasurementUnit::getValue)
      .orElse(null);
    return new MeasurementValue(value, when);
  }
}
