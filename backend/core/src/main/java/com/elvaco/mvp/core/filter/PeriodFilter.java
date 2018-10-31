package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;

public class PeriodFilter extends Filter<SelectionPeriod> {

  private final SelectionPeriod period;

  PeriodFilter(
    Collection<SelectionPeriod> values,
    ComparisonMode comparisonMode,
    SelectionPeriod period
  ) {
    super(values, comparisonMode);
    this.period = period;
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }

  public SelectionPeriod getPeriod() {
    return period;
  }
}
