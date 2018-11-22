package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class WildcardFilter extends Filter<String> {

  WildcardFilter(Collection<String> values, ComparisonMode comparisonMode) {
    super(values, comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
