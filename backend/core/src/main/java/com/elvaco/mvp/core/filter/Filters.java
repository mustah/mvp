package com.elvaco.mvp.core.filter;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Filters implements VisitableFilter {

  private final Collection<VisitableFilter> filters;

  public Filters add(VisitableFilter filter) {
    filters.add(filter);
    return this;
  }

  @Override
  public void accept(FilterVisitor visitor) {
    filters.forEach(filter -> filter.accept(visitor));
  }
}
