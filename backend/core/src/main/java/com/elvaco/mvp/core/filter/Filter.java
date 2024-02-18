package com.elvaco.mvp.core.filter;

import java.util.ArrayList;
import java.util.Collection;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
abstract class Filter<T> implements VisitableFilter {

  private final Collection<T> values;

  Filter(Collection<T> values) {
    if (values.isEmpty()) {
      throw new IllegalArgumentException(
        "No values provided; must have at least one value to filter on"
      );
    }
    this.values = values;
  }

  public Collection<T> values() {
    return new ArrayList<>(values);
  }

  public T oneValue() {
    return new ArrayList<>(values).getFirst();
  }
}
