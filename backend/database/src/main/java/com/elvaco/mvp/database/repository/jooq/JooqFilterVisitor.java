package com.elvaco.mvp.database.repository.jooq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

abstract class JooqFilterVisitor implements FilterAcceptor, FilterVisitor, ConditionAdding, Joins {

  private final Collection<Condition> conditions = new ArrayList<>();
  private final Collection<FilterAcceptor> decorators;

  private List<Joins> joiners = List.of();

  JooqFilterVisitor(Collection<FilterAcceptor> decorators) {
    this.decorators = decorators;
  }

  protected abstract <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query);

  @Override
  public Joins accept(Filters filters) {
    decorateWith(filters);
    filters.accept(this);
    return joinSupplier().get();
  }

  @Override
  public <R extends Record> Joins andJoinsOn(SelectJoinStep<R> query) {
    joinOn(query).where(conditions);
    joiners.forEach(joiner -> joiner.andJoinsOn(query));
    return this;
  }

  @Override
  public void addCondition(@Nullable Condition condition) {
    if (condition != null) {
      conditions.add(condition);
    }
  }

  private void decorateWith(Filters filters) {
    joiners = decorators.stream()
      .map(acceptor -> acceptor.accept(filters))
      .toList();
  }

  private Supplier<Joins> joinSupplier() {
    return () -> this;
  }
}
