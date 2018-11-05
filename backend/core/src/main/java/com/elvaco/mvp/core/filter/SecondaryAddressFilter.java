package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class SecondaryAddressFilter extends Filter<String> {
  SecondaryAddressFilter(
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
