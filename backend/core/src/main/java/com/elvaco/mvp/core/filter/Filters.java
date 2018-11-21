package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class Filters implements VisitableFilter {

  private final Collection<VisitableFilter> filters;

  Filters(Collection<VisitableFilter> filters) {
    this.filters = filters;
  }

  public Filters add(VisitableFilter filter) {
    filters.add(filter);
    return this;
  }

  @Override
  public void accept(FilterVisitor visitor) {
    filters.forEach(filter -> filter.accept(visitor));
  }
}
