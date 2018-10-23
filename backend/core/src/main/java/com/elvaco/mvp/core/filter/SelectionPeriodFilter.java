package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;

public abstract class SelectionPeriodFilter<T> extends Filter<T> {
  private final SelectionPeriod period;

  SelectionPeriodFilter(
    Collection<T> values,
    ComparisonMode comparisonMode,
    SelectionPeriod period
  ) {
    super(values, comparisonMode);
    this.period = period;
  }

  public SelectionPeriod getPeriod() {
    return period;
  }
}
