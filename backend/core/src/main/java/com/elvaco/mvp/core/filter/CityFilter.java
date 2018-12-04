package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class CityFilter extends StringMatchingFilter {

  CityFilter(Collection<String> values, MatchType matchType) {
    super(values, matchType);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
