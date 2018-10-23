package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class FilterSet implements VisitableFilter {

  private final Collection<VisitableFilter> filters;

  FilterSet(Collection<VisitableFilter> filters) {
    this.filters = filters;
  }

  public void add(VisitableFilter filter) {
    filters.add(filter);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    filters.forEach(filter -> filter.accept(visitor));
  }
}
