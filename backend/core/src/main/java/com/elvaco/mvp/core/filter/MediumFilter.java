package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class MediumFilter extends Filter<String> {
  protected MediumFilter(
    Collection<String> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
