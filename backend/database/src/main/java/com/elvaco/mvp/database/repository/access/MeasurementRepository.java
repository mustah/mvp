package com.elvaco.mvp.database.repository.access;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.database.util.SqlErrorMapper;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record10;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.Tables.DISPLAY_QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.atTimeZone;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lead;

public class MeasurementRepository implements Measurements {

  private static final String VALUE_DATE_FIELD_NAME = "value_date";
  private static final String VALUE_FIELD_NAME = "value";
  private static final String QUANTITY_FIELD_NAME = "quantity";

  private final DSLContext dsl;
  private final MeasurementJpaRepository measurementJpaRepository;
  private final QuantityProvider quantityProvider;
  private final UnitConverter unitConverter;
  private final MeasurementEntityMapper measurementEntityMapper;
  private final FilterAcceptor logicalMeterFilters;

  public MeasurementRepository(
    DSLContext dsl,
    MeasurementJpaRepository measurementJpaRepository,
    QuantityProvider quantityProvider,
    UnitConverter unitConverter,
    QuantityEntityMapper quantityEntityMapper,
    FilterAcceptor logicalMeterFilters
  ) {
    this.dsl = dsl;
    this.measurementJpaRepository = measurementJpaRepository;
    this.quantityProvider = quantityProvider;
    this.unitConverter = unitConverter;
    this.logicalMeterFilters = logicalMeterFilters;
    this.measurementEntityMapper = new MeasurementEntityMapper(
      unitConverter,
      quantityProvider,
      quantityEntityMapper
    );
  }

  static List<MeasurementValue> fillMissing(
    List<MeasurementValue> values,
    ZonedDateTime start,
    ZonedDateTime stop,
    TemporalResolution resolution
  ) {
    ZonedDateTime t = resolution.getStart(start);
    if (!t.isEqual(start)) {
      t = resolution.getStart(start.plus(1, resolution));
    }

    Queue<MeasurementValue> queue = new PriorityQueue<>(
      Math.max(values.size(), 1),
      Comparator.comparing((MeasurementValue value) -> value.when)
    );
    queue.addAll(values);

    List<MeasurementValue> filled = new ArrayList<>(values.size());
    while (t.isBefore(stop) || t.isEqual(stop)) {
      if (!queue.isEmpty()) {
        MeasurementValue peek = queue.peek();
        if (peek.when.equals(t.toInstant())) {
          filled.add(queue.remove());
        } else {
          if (peek.when.isBefore(t.toInstant())) {
            queue.remove();
            continue;
          }
          filled.add(new MeasurementValue(null, t.toInstant()));
        }
      } else {
        filled.add(new MeasurementValue(null, t.toInstant()));
      }
      t = t.plus(1, resolution);
    }
    return filled;
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
    Map<String, List<MeasurementValue>> result = parameter.getQuantities()
      .stream()
      .collect(toMap(q -> q.name, q -> new ArrayList<>()));

    var consumptionParameter = getConsumptionParameter(parameter);
    if (!consumptionParameter.getQuantities().isEmpty()) {
      var r = getConsumptionAverageQuery(consumptionParameter);
      Map<String, List<MeasurementValue>> map = mapAverageForPeriod(consumptionParameter, r);
      result.putAll(map);
    }

    var readoutParameter = getReadoutParameter(parameter);
    if (!readoutParameter.getQuantities().isEmpty()) {
      var r = getReadoutAverageQuery(readoutParameter);
      result.putAll(mapAverageForPeriod(readoutParameter, r));
    }

    return result.keySet()
      .stream()
      .collect(toMap(
        identity(),
        key -> fillMissing(
          result.get(key),
          parameter.getFrom(),
          parameter.getTo(),
          parameter.getResolution()
        )
      ));
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

    return result.keySet()
      .stream()
      .collect(toMap(
        identity(),
        key -> fillMissing(
          result.get(key),
          Dates.latest(List.of(
            key.activePeriod.getFirstIncluded().orElse(parameter.getFrom()),
            parameter.getFrom()
          )),
          Dates.earliest(List.of(
            key.activePeriod.getLastIncluded().orElse(parameter.getTo()),
            parameter.getTo()
          )),
          parameter.getResolution()
        )
      ));
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

  private ResultQuery<Record10<
    UUID, String, PeriodRange, String, String, String, String, String, Double, OffsetDateTime
    >> getReadoutSeriesQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);

    var query =
      dsl.select(
        LOGICAL_METER.ID,
        PHYSICAL_METER.ADDRESS,
        PHYSICAL_METER.ACTIVE_PERIOD,
        QUANTITY.NAME,
        LOGICAL_METER.EXTERNAL_ID,
        LOCATION.CITY,
        LOCATION.STREET_ADDRESS,
        MEDIUM.NAME,
        getValueField(false),
        MEASUREMENT.CREATED.as(valueDate)
      ).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameter.getParameters()))
      .andJoinsOn(query);

    return withAdditionalJoins(query, parameter);
  }

  private ResultQuery<Record10<
    UUID, String, PeriodRange, String, String, String, String, String, Double, OffsetDateTime
    >> getConsumptionQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);
    Field<Double> value = field(VALUE_FIELD_NAME, Double.class);
    Field<String> physicalMeterAddress = field("physicalmeter_address", String.class);
    Field<PeriodRange> activePeriod = field("active_period", PeriodRange.class);
    Field<String> quantity = field(QUANTITY_FIELD_NAME, String.class);
    Field<UUID> logicalMeterId = field("logicalmeter_id", UUID.class);
    Field<String> logicalMeterExternalId = field("logicalmeter_external_id", String.class);
    Field<String> city = field("city", String.class);
    Field<String> streetAddress = field("street_address", String.class);
    Field<String> mediumName = field("medium_name", String.class);

    var measurementSeries = dsl.select(
      LOGICAL_METER.ID.as(logicalMeterId),
      PHYSICAL_METER.ADDRESS.as(physicalMeterAddress),
      PHYSICAL_METER.ACTIVE_PERIOD.as(activePeriod),
      QUANTITY.NAME.as(quantity),
      LOGICAL_METER.EXTERNAL_ID.as(logicalMeterExternalId),
      LOCATION.CITY.as(city),
      LOCATION.STREET_ADDRESS.as(streetAddress),
      MEDIUM.NAME.as(mediumName),
      getValueField(true).as(value),
      MEASUREMENT.CREATED.as(valueDate)
    ).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameter.getParameters()))
      .andJoinsOn(measurementSeries);

    OffsetDateTime start = parameter.getResolution()
      .getStart(parameter.getFrom()).toOffsetDateTime();

    OffsetDateTime stop = parameter.getResolution()
      .getStart(parameter.getTo()).toOffsetDateTime();

    Condition condition = valueDate.greaterOrEqual(start)
      .and(valueDate.lessOrEqual(stop));

    if (start.isEqual(stop)) {
      condition = valueDate.eq(start);
    }

    return dsl.select(
      logicalMeterId,
      physicalMeterAddress,
      activePeriod,
      quantity,
      logicalMeterExternalId,
      city,
      streetAddress,
      mediumName,
      value,
      valueDate
    ).from(withAdditionalJoins(measurementSeries, parameter))
      .where(condition)
      .orderBy(valueDate.asc());
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getReadoutAverageQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);

    var query = dsl.select(
      avg(MEASUREMENT.VALUE),
      QUANTITY.NAME,
      MEASUREMENT.CREATED.as(valueDate)
    ).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameter.getParameters()))
      .andJoinsOn(query);

    query = withAdditionalJoins(query, parameter);

    return query.groupBy(valueDate, QUANTITY.NAME)
      .orderBy(valueDate, QUANTITY.NAME);
  }

  private ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> getConsumptionAverageQuery(
    MeasurementParameter parameter
  ) {
    Field<OffsetDateTime> valueDate = field(VALUE_DATE_FIELD_NAME, OffsetDateTime.class);
    Field<Double> value = field(VALUE_FIELD_NAME, Double.class);
    Field<String> quantity = field(QUANTITY_FIELD_NAME, String.class);

    var query = dsl.select(
      getValueField(true).as(value),
      QUANTITY.NAME.as(quantity),
      MEASUREMENT.CREATED.as(valueDate)
    ).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameter.getParameters()))
      .andJoinsOn(query);

    return dsl.select(
      avg(value),
      quantity,
      valueDate
    ).from(withAdditionalJoins(query, parameter))
      .where(valueDate.lessOrEqual(parameter.getResolution()
        .getStart(parameter.getTo())
        .toOffsetDateTime()))
      .groupBy(valueDate, quantity)
      .orderBy(valueDate, quantity);
  }

  private <T extends Record> SelectOnConditionStep<T> withAdditionalJoins(
    SelectJoinStep<T> query,
    MeasurementParameter parameter
  ) {
    Condition quantityCondition = parameter.getQuantities().isEmpty()
      ? falseCondition()
      : QUANTITY.NAME.in(parameter.getQuantities()
        .stream()
        .map(q -> q.name)
        .collect(toList()));

    OffsetDateTime stop = parameter.getTo().toOffsetDateTime();

    if (parameter.getQuantities().get(0).isConsumption()) {
      stop = stop.plus(1, parameter.getResolution());
    }

    return query
      .innerJoin(DISPLAY_QUANTITY)
      .on(METER_DEFINITION.ID.eq(DISPLAY_QUANTITY.METER_DEFINITION_ID))
      .innerJoin(QUANTITY)
      .on(DISPLAY_QUANTITY.QUANTITY_ID.eq(QUANTITY.ID).and(quantityCondition))
      .innerJoin(MEASUREMENT)
      .on(MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
        .and(periodContains(PHYSICAL_METER.ACTIVE_PERIOD, MEASUREMENT.CREATED))
        .and(
          MEASUREMENT.CREATED.eq(atTimeZone(
            iso8601OffsetToPosixOffset(LOGICAL_METER.UTC_OFFSET),
            DSL.trunc(
              atTimeZone(iso8601OffsetToPosixOffset(LOGICAL_METER.UTC_OFFSET), MEASUREMENT.CREATED),
              toDatePart(parameter.getResolution())
            )
          ))
        ).and(MEASUREMENT.CREATED.lessOrEqual(stop))
        .and(MEASUREMENT.CREATED.greaterOrEqual(parameter.getFrom().toOffsetDateTime()))
        .and(MEASUREMENT.QUANTITY.equal(QUANTITY.ID)));
  }

  private DatePart toDatePart(TemporalResolution resolution) {
    if (resolution == TemporalResolution.hour) {
      return DatePart.HOUR;
    } else if (resolution == TemporalResolution.day) {
      return DatePart.DAY;
    } else if (resolution == TemporalResolution.month) {
      return DatePart.MONTH;
    }
    throw new IllegalArgumentException("Unknown resolution: " + resolution.toString());
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
        .filter(QuantityParameter::isConsumption)
        .collect(toList()))
      .build();
  }

  private Map<MeasurementKey, List<MeasurementValue>> mapSeriesForPeriod(
    MeasurementParameter parameter,
    ResultQuery<Record10<
      UUID, String, PeriodRange, String, String, String, String, String, Double, OffsetDateTime
      >> r
  ) {
    Map<String, QuantityParameter> quantityMap = getQuantityMap(parameter);

    return r.fetch().stream()
      .collect(groupingBy(
        k -> new MeasurementKey(
          k.value1(),
          k.value2(),
          k.value3(),
          k.value4(),
          k.value5(),
          k.value6(),
          k.value7(),
          k.value8()
        ),
        mapping(
          t -> toMeasurementValueConvertedToUnitFromQuantity(
            t.get(t.field10()).toInstant(),
            t.get(t.field9()),
            quantityMap.get(t.get(t.field4()))
          ), toList())
        )
      );
  }

  private Map<String, List<MeasurementValue>> mapAverageForPeriod(
    MeasurementParameter parameter,
    ResultQuery<Record3<BigDecimal, String, OffsetDateTime>> r
  ) {
    Map<String, QuantityParameter> quantityMap = getQuantityMap(parameter);

    Result<Record3<BigDecimal, String, OffsetDateTime>> fetch = r.fetch();
    return fetch.stream()
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

  private static Field<String> iso8601OffsetToPosixOffset(Field<String> iso8600Offset) {
    return iso8600Offset.cast(Integer.class).neg().cast(String.class);
  }
}
