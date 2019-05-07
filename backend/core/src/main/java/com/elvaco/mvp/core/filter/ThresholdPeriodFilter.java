package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;

public class ThresholdPeriodFilter extends Filter<FilterPeriod> {

  private final FilterPeriod period;

  ThresholdPeriodFilter(
    Collection<FilterPeriod> values,
    FilterPeriod period
  ) {
    super(values);
    this.period = period;
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }

  public FilterPeriod getPeriod() {
    return period;
  }
}
