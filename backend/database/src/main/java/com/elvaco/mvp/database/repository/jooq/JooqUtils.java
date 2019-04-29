package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.entity.jooq.tables.records.MeasurementStatDataRecord;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

@UtilityClass
public class JooqUtils {

  public static final Field<Double> COLLECTION_PERCENTAGE = field(
    "collection_percentage",
    Double.class
  );
  public static final Field<OffsetDateTime> LAST_DATA = field(
    "last_data",
    OffsetDateTime.class
  );
  static final Table<?> METER_STATS = table("meter_stats");

  public static Condition periodContains(Field<PeriodRange> field, OffsetDateTime time) {
    return condition("range_contains_elem({0}, {1})", field, val(time));
  }

  public static Condition periodContains(Field<PeriodRange> field, Field<OffsetDateTime> time) {
    return condition("range_contains_elem({0}, {1})", field, time);
  }

  public static Condition periodOverlaps(Field<PeriodRange> field, PeriodRange range) {
    return condition("range_overlaps({0}, {1})", field, range);
  }

  static Condition valueConditionFor(MeasurementThreshold threshold) {
    Field<Double> max = threshold.quantity.isConsumptionSeries()
      ? asHourly(MEASUREMENT_STAT_DATA.MAX)
      : MEASUREMENT_STAT_DATA.MAX;

    Field<Double> min = threshold.quantity.isConsumptionSeries()
      ? asHourly(MEASUREMENT_STAT_DATA.MIN)
      : MEASUREMENT_STAT_DATA.MIN;

    if (threshold.duration == null) {
      return getCondition(threshold, max, min);
    } else {
      return getCondition(threshold, min, max);
    }
  }

  static Condition measurementStatsConditionFor(FilterPeriod period) {
    LocalDate startDate = period.start.toLocalDate();
    LocalDate stopDate = period.stop.toLocalDate();

    if (stopDate.isEqual(startDate)) {
      return MEASUREMENT_STAT_DATA.STAT_DATE.equal(Date.valueOf(startDate))
        .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    } else {
      return MEASUREMENT_STAT_DATA.STAT_DATE.greaterOrEqual(Date.valueOf(startDate))
        .and(MEASUREMENT_STAT_DATA.STAT_DATE.lessThan(Date.valueOf(stopDate)))
        .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    }
  }

  public static Table<Record> dateSerieFor(
    LocalDate from,
    LocalDate to,
    String resolution,
    String valueFieldName
  ) {
    String expr = "generate_series({0},"
      + "{1},"
      + "{2}::interval) as " + valueFieldName;
    return table(
      expr,
      from,
      to,
      resolution
    );
  }

  public static Field<OffsetDateTime> atTimeZone(
    Field<String> tzField,
    Field<OffsetDateTime> field
  ) {
    return DSL.field("timezone({0}, {1})", OffsetDateTime.class, tzField, field);
  }

  private static Condition getCondition(
    MeasurementThreshold threshold,
    Field<Double> max,
    Field<Double> min
  ) {
    switch (threshold.operator) {
      case LESS_THAN:
        return min.lessThan(threshold.getConvertedValue());
      case LESS_THAN_OR_EQUAL:
        return min.lessOrEqual(threshold.getConvertedValue());
      case GREATER_THAN:
        return max.greaterThan(threshold.getConvertedValue());
      case GREATER_THAN_OR_EQUAL:
        return max.greaterOrEqual(threshold.getConvertedValue());
      default:
        throw new UnsupportedOperationException(String.format(
          "Measurement threshold operator '%s' is not supported",
          threshold.operator.name()
        ));
    }
  }

  private static Field<Double> asHourly(TableField<MeasurementStatDataRecord, Double> field) {
    return field.divide(nullif(PHYSICAL_METER.READ_INTERVAL_MINUTES, 0L).divide(inline(60.0)));
  }
}

