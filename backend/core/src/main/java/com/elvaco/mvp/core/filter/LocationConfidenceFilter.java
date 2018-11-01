package com.elvaco.mvp.core.filter;

import static java.util.Collections.singletonList;

public class LocationConfidenceFilter extends Filter<Double> {
  public LocationConfidenceFilter(
    double value,
    ComparisonMode comparisonMode
  ) {
    super(singletonList(value), comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
