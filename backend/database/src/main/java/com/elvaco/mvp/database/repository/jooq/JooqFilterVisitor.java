package com.elvaco.mvp.database.repository.jooq;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;

public abstract class JooqFilterVisitor implements FilterVisitor {

  private final Collection<Condition> conditions = new ArrayList<>();

  protected abstract <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query);

  public <R extends Record> SelectConditionStep<R> apply(
    Filters filters,
    SelectJoinStep<R> query
  ) {
    filters.accept(this);
    return applyJoins(query).where(conditions);
  }

  void addCondition(@Nullable Condition condition) {
    if (condition != null) {
      conditions.add(condition);
    }
  }

  protected Condition valueConditionFor(MeasurementThreshold threshold) {
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
