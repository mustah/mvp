package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.domainmodels.StatusType;

public class StatusTypeSelectionPeriodFilter extends SelectionPeriodFilter<StatusType> {

  StatusTypeSelectionPeriodFilter(
    Collection<StatusType> values,
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
