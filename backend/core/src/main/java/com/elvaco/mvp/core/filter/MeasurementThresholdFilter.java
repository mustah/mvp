package com.elvaco.mvp.core.filter;

import java.util.Collections;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MeasurementThresholdFilter extends Filter<String> {

  MeasurementThresholdFilter(
    String value
  ) {
    super(Collections.singleton(value));
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
