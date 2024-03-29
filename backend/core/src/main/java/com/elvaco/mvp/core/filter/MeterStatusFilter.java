package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.StatusType;

public class MeterStatusFilter extends Filter<StatusType> {

  MeterStatusFilter(
    Collection<StatusType> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
