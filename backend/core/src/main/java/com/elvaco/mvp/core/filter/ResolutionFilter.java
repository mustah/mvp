package com.elvaco.mvp.core.filter;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.TemporalResolution;

public class ResolutionFilter extends Filter<TemporalResolution> {
  ResolutionFilter(
    Collection<TemporalResolution> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
