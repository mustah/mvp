package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static org.jooq.impl.DSL.field;

@UtilityClass
public class JooqUtils {

  public static final Field<Long> MISSING_MEASUREMENT_COUNT = field(
    "missing_measurement_count",
    Long.class
  );

  static Condition valueConditionFor(MeasurementThreshold threshold) {
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
  }
}
