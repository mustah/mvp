package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class CityFilter extends Filter<String> {

  private final boolean isWildcard;

  CityFilter(Collection<String> values, boolean isWildcard) {
    super(values);
    this.isWildcard = isWildcard;
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }

  public boolean isWildcard() {
    return isWildcard;
  }
}
