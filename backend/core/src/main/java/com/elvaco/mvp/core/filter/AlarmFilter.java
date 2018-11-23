package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class AlarmFilter extends Filter<String> {
  AlarmFilter(
    Collection<String> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
