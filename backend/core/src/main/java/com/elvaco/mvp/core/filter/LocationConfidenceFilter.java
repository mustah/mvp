package com.elvaco.mvp.core.filter;

import static java.util.Collections.singletonList;

public class LocationConfidenceFilter extends Filter<Double> {

  public LocationConfidenceFilter(double value) {
    super(singletonList(value));
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
