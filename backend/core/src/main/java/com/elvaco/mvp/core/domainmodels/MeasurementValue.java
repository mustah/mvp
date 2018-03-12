package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;

public class MeasurementValue {

  public final Instant when;
  public final Double value;

  public MeasurementValue(Double value, Instant when) {
    this.value = value;
    this.when = when;
  }
}
