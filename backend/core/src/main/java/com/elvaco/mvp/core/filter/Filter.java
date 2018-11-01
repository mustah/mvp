package com.elvaco.mvp.core.filter;

import java.util.ArrayList;
import java.util.Collection;

abstract class Filter<T> implements VisitableFilter {
  private final Collection<T> values;
  private final ComparisonMode comparisonMode;

  Filter(Collection<T> values, ComparisonMode comparisonMode) {
    if (values.isEmpty()) {
      throw new IllegalArgumentException(
        "No values provided; must have at least one value to filter on"
      );
    }
    this.values = values;
    this.comparisonMode = comparisonMode;
  }

  public Collection<T> values() {
    return values;
  }

  public T oneValue() {
    return new ArrayList<>(values).get(0);
  }

  public ComparisonMode mode() {
    return comparisonMode;
  }
}
