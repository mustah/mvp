package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;

// TODO replace with ThresholdPeriodFilter & ReportPeriodFilter
public class PeriodFilter extends Filter<FilterPeriod> {

  private final FilterPeriod period;

  PeriodFilter(
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
