package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class ManufacturerFilter extends Filter<String> {
  ManufacturerFilter(
    Collection<String> values,
    ComparisonMode comparisonMode
  ) {
    super(values, comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
