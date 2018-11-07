package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;

abstract class FilterQueryDslJpaVisitor implements FilterVisitor {
  static final Predicate FALSE_PREDICATE = Expressions.asBoolean(true).isFalse();

  abstract Collection<Predicate> getPredicates();

  abstract void applyJoins(JPQLQuery<?> q);

  final void visitAndApply(Filters filters, JPQLQuery<?>... query) {
    filters.accept(this);
    for (JPQLQuery<?> q : query) {
      applyJoins(q);
      q.where(ExpressionUtils.allOf(getPredicates()));
    }
  }

  BooleanExpression withinPeriod(
    SelectionPeriod period,
    DateTimePath<ZonedDateTime> start,
    DateTimePath<ZonedDateTime> stop
  ) {
    return start.before(period.stop)
      .and(stop.isNull().or(stop.after(period.start)));
  }
}
