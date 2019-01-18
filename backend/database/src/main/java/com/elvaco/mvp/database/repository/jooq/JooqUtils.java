package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.entity.jooq.tables.records.MeasurementStatDataRecord;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.TableField;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.val;

@UtilityClass
public class JooqUtils {

  public static final Field<Long> MISSING_MEASUREMENT_COUNT = field(
    "missing_measurement_count",
    Long.class
  );

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

    Field<Double> max = threshold.quantity.isConsumption()
      ? asHourly(MEASUREMENT_STAT_DATA.MAX)
      : MEASUREMENT_STAT_DATA.MAX;

    Field<Double> min = threshold.quantity.isConsumption()
      ? asHourly(MEASUREMENT_STAT_DATA.MIN)
      : MEASUREMENT_STAT_DATA.MIN;

    if (threshold.duration == null) {
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
    } else {
      switch (threshold.operator) {
        case LESS_THAN:
          return max.lessThan(threshold.getConvertedValue());
        case LESS_THAN_OR_EQUAL:
          return max.lessOrEqual(threshold.getConvertedValue());
        case GREATER_THAN:
          return min.greaterThan(threshold.getConvertedValue());
        case GREATER_THAN_OR_EQUAL:
          return min.greaterOrEqual(threshold.getConvertedValue());
        default:
          throw new UnsupportedOperationException(String.format(
            "Measurement threshold operator '%s' is not supported",
            threshold.operator.name()
          ));
      }
    }
  }

  private static Field<Double> asHourly(TableField<MeasurementStatDataRecord, Double> field) {
    return field.divide(PHYSICAL_METER.READ_INTERVAL_MINUTES.divide(inline(60.0)));
  }
}

