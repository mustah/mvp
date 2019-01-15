package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectionPeriodTest {
  private static final ZonedDateTime START = ZonedDateTime.parse("2018-04-01T00:00:00Z");
  private static final ZonedDateTime STOP = ZonedDateTime.parse("2018-04-02T00:00:00Z");

  @Test
  public void toPeriodRange_isHalfOpen() {
    assertThat(new SelectionPeriod(START, STOP).toPeriodRange())
      .isEqualTo(PeriodRange.halfOpenFrom(START, STOP));
  }
}
