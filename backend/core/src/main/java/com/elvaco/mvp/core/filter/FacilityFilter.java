package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class FacilityFilter extends Filter<String> {
  FacilityFilter(
    Collection<String> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
