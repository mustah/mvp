package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SelectionPeriod {
  public final ZonedDateTime start;
  public final ZonedDateTime stop;

  public PeriodRange toPeriodRange() {
    return PeriodRange.halfOpenFrom(start, stop);
  }
}
