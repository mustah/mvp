package com.elvaco.mvp.core.filter;

public class OrganisationParentFilter implements VisitableFilter {
  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
