package com.elvaco.mvp.core.filter;

import java.util.Collection;
import java.util.UUID;

public class LogicalMeterIdFilter extends IdFilter {
  protected LogicalMeterIdFilter(
    Collection<UUID> values,
    ComparisonMode comparisonMode
  ) {
    super(values, comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
