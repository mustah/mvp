package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.TemporalResolution;

public class ResolutionFilter extends Filter<TemporalResolution> {
  ResolutionFilter(
    Collection<TemporalResolution> values,
    ComparisonMode comparisonMode
  ) {
    super(values, comparisonMode);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
