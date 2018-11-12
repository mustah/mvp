package com.elvaco.mvp.database.repository.querydsl;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

import static java.util.Collections.emptyList;

public class MissingMeasurementFilterQueryDslVisitor extends EmptyFilterQueryDslJpaVisitor {

  private Predicate condition = FALSE_PREDICATE;

  @Override
  public void visit(PeriodFilter periodFilter) {
    SelectionPeriod period = periodFilter.getPeriod();

    condition = MISSING_MEASUREMENT.id.expectedTime.lt(period.stop)
      .and(MISSING_MEASUREMENT.id.expectedTime.goe(period.start));
  }

  @Override
  protected void applyJoins(JPQLQuery<?> q) {
    q.leftJoin(PHYSICAL_METER.missingMeasurements, MISSING_MEASUREMENT).on(condition);
  }

  @Override
  protected Collection<Predicate> getPredicates() {
    return emptyList();
  }
}
