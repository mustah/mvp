package com.elvaco.mvp.core.filter;

import java.util.Collection;
import java.util.UUID;

public class OrganisationIdFilter extends IdFilter {
  OrganisationIdFilter(
    Collection<UUID> values,
    ComparisonMode comparisonMode
  ) {
    super(values, comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
