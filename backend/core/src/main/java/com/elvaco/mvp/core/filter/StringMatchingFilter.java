package com.elvaco.mvp.core.filter;

import java.util.Collection;

abstract class StringMatchingFilter extends Filter<String> {

  private final boolean isWildcard;

  StringMatchingFilter(Collection<String> values, MatchType matchType) {
    super(values);
    this.isWildcard = MatchType.WILDCARD.equals(matchType);
  }

  public boolean isWildcard() {
    return isWildcard;
  }
}
