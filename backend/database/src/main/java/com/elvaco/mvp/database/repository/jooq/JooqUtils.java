package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.PeriodRange;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
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

  public static Condition periodOverlaps(Field<PeriodRange> field, PeriodRange range) {
    return condition("range_overlaps({0}, {1})", field, range);
  }

  static Condition valueConditionFor(MeasurementThreshold threshold) {
    if (threshold.duration == null) {
      switch (threshold.operator) {
        case LESS_THAN:
          return MEASUREMENT_STAT_DATA.MIN.lessThan(threshold.getConvertedValue());
        case LESS_THAN_OR_EQUAL:
          return MEASUREMENT_STAT_DATA.MIN.lessOrEqual(threshold.getConvertedValue());
        case GREATER_THAN:
          return MEASUREMENT_STAT_DATA.MAX.greaterThan(threshold.getConvertedValue());
        case GREATER_THAN_OR_EQUAL:
          return MEASUREMENT_STAT_DATA.MAX.greaterOrEqual(threshold.getConvertedValue());
        default:
          throw new UnsupportedOperationException(String.format(
            "Measurement threshold operator '%s' is not supported",
            threshold.operator.name()
          ));
      }
    } else {
      switch (threshold.operator) {
        case LESS_THAN:
          return MEASUREMENT_STAT_DATA.MAX.lessThan(threshold.getConvertedValue());
        case LESS_THAN_OR_EQUAL:
          return MEASUREMENT_STAT_DATA.MAX.lessOrEqual(threshold.getConvertedValue());
        case GREATER_THAN:
          return MEASUREMENT_STAT_DATA.MIN.greaterThan(threshold.getConvertedValue());
        case GREATER_THAN_OR_EQUAL:
          return MEASUREMENT_STAT_DATA.MIN.greaterOrEqual(threshold.getConvertedValue());
        default:
          throw new UnsupportedOperationException(String.format(
            "Measurement threshold operator '%s' is not supported",
            threshold.operator.name()
          ));
      }

    }
  }
}

