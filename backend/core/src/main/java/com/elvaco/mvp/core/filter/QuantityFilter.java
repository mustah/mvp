package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.Quantity;

public class QuantityFilter extends Filter<Quantity> {
  QuantityFilter(
    Collection<Quantity> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
