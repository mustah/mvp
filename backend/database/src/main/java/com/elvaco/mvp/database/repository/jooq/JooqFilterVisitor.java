package com.elvaco.mvp.database.repository.jooq;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;

abstract class JooqFilterVisitor implements FilterAcceptor, FilterVisitor, ConditionAdding, Joins {

  private final Collection<Condition> conditions = new ArrayList<>();

  protected abstract <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query);

  @Override
  public Joins apply(Filters filters) {
    filters.accept(this);
    return new DelegateJoiner(conditions, this);
  }

  @Override
  public <R extends Record> Joins applyJoinsOn(SelectJoinStep<R> query) {
    return new DelegateJoiner(conditions, this).applyJoinsOn(query);
  }

  @Override
  public void addCondition(@Nullable Condition condition) {
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

  /**
   * This class is needed since the injected proxy instances clears <code>conditions</code>
   * collections every time the proxy instance is accessed. So see this class as just a delegate to
   * reuse visited filters once and then apply joins using these filters to queries in a more
   * fluent manner.
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static class DelegateJoiner implements Joins {

    private final Collection<Condition> conditions;
    private final JooqFilterVisitor filterVisitor;

    @Override
    public <R extends Record> Joins applyJoinsOn(SelectJoinStep<R> query) {
      filterVisitor.applyJoins(query).where(conditions);
      return filterVisitor;
    }
  }
}
