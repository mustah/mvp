package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeasurementValue {

  public final Double value;
  public final Instant when;
}
