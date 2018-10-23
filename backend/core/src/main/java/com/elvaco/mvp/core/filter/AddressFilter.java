package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class AddressFilter extends Filter<String> {
  AddressFilter(
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
