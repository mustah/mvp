package com.elvaco.mvp.core.filter;

import java.util.Collection;

public class WildcardFilter extends Filter<String> {

  WildcardFilter(Collection<String> values) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
