package com.elvaco.mvp.database.repository.jooq;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

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
}
