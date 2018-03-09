package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MeasurementValue {

  public Instant when;
  public Double value;

  public MeasurementValue(Double value, Instant when) {
    this.value = value;
    this.when = when;
  }
}
