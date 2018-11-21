package com.elvaco.mvp.core.filter;

public enum ComparisonMode {
  EQUAL,
  WILDCARD;

  public boolean isWildcard() {
    return this == WILDCARD;
  }
}
