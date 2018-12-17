package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodBoundTest {

  private static final ZonedDateTime UTC_TIME = ZonedDateTime.parse("2018-01-02T00:00:00+00");
  private static final ZonedDateTime UTC_PLUS_ONE_TIME =
    ZonedDateTime.parse("2018-01-02T01:00:00+01");

  @Test
  public void equals_sameDateTimeIsEqual() {
    assertThat(PeriodBound.inclusiveOf(UTC_TIME))
      .isEqualTo(PeriodBound.inclusiveOf(UTC_PLUS_ONE_TIME));
  }

  @Test
  public void equals_differentDateTimeSameInstantIsEqual() {
    assertThat(PeriodBound.inclusiveOf(UTC_TIME))
      .isEqualTo(PeriodBound.inclusiveOf(UTC_PLUS_ONE_TIME));
  }

  @Test
  public void equals_sameInstantDifferentInclusivenessIsNotEqual() {
    assertThat(PeriodBound.exclusiveOf(UTC_TIME))
      .isNotEqualTo(PeriodBound.inclusiveOf(UTC_TIME));
  }

  @Test
  public void equals_nullCases() {
    assertThat(PeriodBound.inclusiveOf(null))
      .isEqualTo(PeriodBound.inclusiveOf(null));

    assertThat(PeriodBound.inclusiveOf(UTC_TIME))
      .isNotEqualTo(PeriodBound.inclusiveOf(null));

    assertThat(PeriodBound.inclusiveOf(null))
      .isNotEqualTo(PeriodBound.inclusiveOf(UTC_TIME));
  }
}
