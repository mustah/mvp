package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class AddressFilter extends Filter<String> {

  private final boolean isWildcard;

  AddressFilter(Collection<String> values, MatchType matchType) {
    super(values);
    this.isWildcard = MatchType.WILDCARD.equals(matchType);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }

  public boolean isWildcard() {
    return isWildcard;
  }
}
