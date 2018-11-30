package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class ManufacturerFilter extends Filter<String> {
  ManufacturerFilter(
    Collection<String> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
