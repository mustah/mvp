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
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
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
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Record5;
import org.jooq.ResultQuery;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableOnConditionStep;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.entity.jooq.Tables.DISPLAY_QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lead;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.trueCondition;

public class MeasurementRepository implements Measurements {

  private static final String VALUE_DATE_FIELD_NAME = "value_date";
  private static final String VALUE_FIELD_NAME = "value";
  private static final String QUANTITY_FIELD_NAME = "quantity";

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
      Quantity quantity = quantityProvider.getByNameOrThrow(measurement.quantity);

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
      var r = getConsumptionQuery(consumptionParameter);
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

  private ResultQuery<Record5<UUID, String, String, Double, OffsetDateTime>> getReadoutSeriesQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = DSL.field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);

    return dsl.select(
      LOGICAL_METER.ID,
      PHYSICAL_METER.ADDRESS,
      QUANTITY.NAME,
      getValueField(false),
      valueDate
    ).from(joinedSourceTables(parameter, VALUE_DATE_FIELD_NAME))
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));
  }

  private ResultQuery<Record5<UUID, String, String, Double, OffsetDateTime>> getConsumptionQuery(
    MeasurementParameter parameter
  ) {
    String physicalMeterFieldName = "physicalmeter_address";
    String logicalMeterFieldName = "logicalmeter";

    Field<OffsetDateTime> valueDate = DSL.field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);
    Field<Double> value = DSL.field(VALUE_FIELD_NAME, Double.class);
    Field<String> physicalMeterAddress = DSL.field(physicalMeterFieldName, String.class);
    Field<String> quantity = DSL.field(QUANTITY_FIELD_NAME, String.class);
    Field<UUID> logicalMeterId = DSL.field(logicalMeterFieldName, UUID.class);

    var measurementSeries = dsl.select(
      LOGICAL_METER.ID.as(logicalMeterId),
      PHYSICAL_METER.ADDRESS.as(physicalMeterAddress),
      QUANTITY.NAME.as(quantity),
      getValueField(true).as(value),
      valueDate
    ).from(joinedSourceTables(parameter, VALUE_DATE_FIELD_NAME))
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));

    return dsl.select(
      logicalMeterId,
      physicalMeterAddress,
      quantity,
      value,
      valueDate
    ).from(measurementSeries)
      .where(
        valueDate.greaterOrEqual(parameter.getResolution().getStart(parameter.getFrom())),
        valueDate.lessOrEqual(parameter.getResolution().getStart(parameter.getTo()))
      )
      .orderBy(valueDate.asc());
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getReadoutAverageQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = DSL.field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);

    return dsl.select(
      avg(MEASUREMENT.VALUE),
      QUANTITY.NAME,
      valueDate
    ).from(joinedSourceTables(parameter, VALUE_DATE_FIELD_NAME))
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()))
      .groupBy(valueDate, QUANTITY.NAME)
      .orderBy(valueDate, QUANTITY.NAME);
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getConsumptionAverageQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = DSL.field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);
    Field<Double> value = DSL.field(VALUE_FIELD_NAME, Double.class);
    Field<String> quantity = DSL.field(QUANTITY_FIELD_NAME, String.class);

    var series = dsl.select(
      getValueField(true).as(value),
      QUANTITY.NAME.as(quantity),
      valueDate
    ).from(joinedSourceTables(parameter, VALUE_DATE_FIELD_NAME))
      .where(LOGICAL_METER.ID.in(parameter.getLogicalMeterIds()));

    return dsl.select(
      avg(value),
      quantity,
      valueDate
    ).from(series)
      .where(valueDate.lessOrEqual(parameter.getResolution().getStart(parameter.getTo())))
      .groupBy(valueDate, quantity)
      .orderBy(valueDate, quantity);
  }

  private TableOnConditionStep<Record> joinedSourceTables(
    MeasurementParameter parameter,
    String dateFieldName
  ) {
    Table<Record> dateSerieTable = dateSerieFor(parameter);
    SelectJoinStep<Record1<OffsetDateTime>> joinStep = select(field(
      dateFieldName + " at time zone 'UTC'",
      OffsetDateTime.class
    ).as(dateFieldName)).from(dateSerieTable);

    Condition quantityCondition = parameter.getQuantities().isEmpty()
      ? trueCondition()
      : QUANTITY.NAME.in(parameter.getQuantities()
        .stream()
        .map(q -> q.name)
        .collect(toList()));

    Field<OffsetDateTime> valueDate = joinStep.field(dateFieldName, OffsetDateTime.class);
    return LOGICAL_METER
      .join(METER_DEFINITION)
      .on(LOGICAL_METER.METER_DEFINITION_ID.eq(METER_DEFINITION.ID))
      .innerJoin(DISPLAY_QUANTITY)
      .on(METER_DEFINITION.ID.eq(DISPLAY_QUANTITY.METER_DEFINITION_ID))
      .innerJoin(QUANTITY).on(DISPLAY_QUANTITY.QUANTITY_ID.eq(QUANTITY.ID).and(quantityCondition))
      .innerJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ID.eq(PHYSICAL_METER.LOGICAL_METER_ID)
        .and(LOGICAL_METER.ORGANISATION_ID.eq(PHYSICAL_METER.ORGANISATION_ID)))
      .rightOuterJoin(joinStep)
      .on(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, valueDate))
      .leftJoin(MEASUREMENT)
      .on(MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
        .and(MEASUREMENT.CREATED.equal(valueDate))
        .and(MEASUREMENT.QUANTITY.equal(QUANTITY.ID)));
  }

  private Field<Double> getValueField(boolean isConsumption) {
    if (isConsumption) {
      return lead(MEASUREMENT.VALUE).over()
        .partitionBy(PHYSICAL_METER.ID, MEASUREMENT.QUANTITY)
        .orderBy(MEASUREMENT.CREATED.asc())
        .minus(MEASUREMENT.VALUE);
    } else {
      return MEASUREMENT.VALUE;
    }
  }

  private Table<Record> dateSerieFor(MeasurementParameter parameter) {
    String expr;
    if (parameter.getQuantities().get(0).isConsumption()) {
      expr = "generate_series({0} at time zone 'UTC',"
        + "{1} at time zone 'UTC' + cast({2} as interval),"
        + "{2}::interval) as " + VALUE_DATE_FIELD_NAME;
    } else {
      expr = "generate_series({0} at time zone 'UTC',"
        + "{1} at time zone 'UTC',"
        + "{2}::interval) as " + VALUE_DATE_FIELD_NAME;
    }
    return DSL.table(
      expr,
      parameter.getResolution().getStart(parameter.getFrom()),
      parameter.getResolution().getStart(parameter.getTo()),
      parameter.getResolution().asInterval()
    );
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
    ResultQuery<Record5<UUID, String, String, Double, OffsetDateTime>> r
  ) {
    Map<String, QuantityParameter> quantityMap = getQuantityMap(parameter);

    return r.fetch().stream()
      .collect(groupingBy(
        k -> new MeasurementKey(k.get(k.field1()), k.get(k.field2()), k.get(k.field3())),
        mapping(
          t -> toMeasurementValueConvertedToUnitFromQuantity(
            t.get(t.field5()).toInstant(),
            t.get(t.field4()),
            quantityMap.get(t.get(t.field3()))
          ), toList())
        )
      );
  }

  private Map<String, List<MeasurementValue>> mapAverageForPeriod(
    MeasurementParameter parameter,
    ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> r
  ) {
    Map<String, QuantityParameter> quantityMap = getQuantityMap(parameter);

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

  private Map<String, QuantityParameter> getQuantityMap(MeasurementParameter parameter) {
    return parameter.getQuantities()
      .stream()
      .collect(toMap(q -> q.name, q -> q));
  }

  private MeasurementValue toMeasurementValueConvertedToUnitFromQuantity(
    Instant when,
    Number fromValue,
    QuantityParameter quantity
  ) {
    Double value = Optional.ofNullable(fromValue)
      .map(unitValue ->
        new MeasurementUnit(
          quantityProvider.getByNameOrThrow(quantity.name).storageUnit,
          unitValue.doubleValue()
        ))
      .map(measurementUnit ->
        Optional.ofNullable(quantity.unit)
          .map(u -> unitConverter.convert(measurementUnit, quantity.unit))
          .orElse(measurementUnit)
      )
      .map(MeasurementUnit::getValue)
      .orElse(null);
    return new MeasurementValue(value, when);
  }
}
