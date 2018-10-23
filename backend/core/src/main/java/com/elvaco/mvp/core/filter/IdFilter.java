package com.elvaco.mvp.core.filter;

import java.util.Collection;
import java.util.UUID;

public abstract class IdFilter extends Filter<UUID> {
  protected IdFilter(
    Collection<UUID> values,
    ComparisonMode comparisonMode
  ) {
    super(values, comparisonMode);
  }
}
