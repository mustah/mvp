package com.elvaco.mvp.core.filter;

import java.util.Collection;
import java.util.Optional;

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

  public <T extends VisitableFilter> Optional<T> find(Class<T> filterClass) {
    return filters.stream()
      .filter(visitableFilter -> visitableFilter.getClass().equals(filterClass))
      .map(visitableFilter -> (T) visitableFilter)
      .findAny();
  }
}
