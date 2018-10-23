package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;

public class PeriodFilter extends SelectionPeriodFilter<SelectionPeriod> {
  PeriodFilter(
    Collection<SelectionPeriod> values,
    ComparisonMode comparisonMode,
    SelectionPeriod period
  ) {
    super(values, comparisonMode, period);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
