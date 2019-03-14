package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class MeasurementValue {

  public final Double value;
  public final Instant when;
}
