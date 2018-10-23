package com.elvaco.mvp.core.filter;

public interface VisitableFilter {

  void accept(FilterVisitor visitor);
}
