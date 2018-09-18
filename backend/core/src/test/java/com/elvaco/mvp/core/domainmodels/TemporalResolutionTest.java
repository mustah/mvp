package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.TemporalResolution.defaultResolutionFor;
import static org.assertj.core.api.Assertions.assertThat;

public class TemporalResolutionTest {

  @Test
  public void lessThanADayResolvesToHourResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.plusHours(23);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.hour);
  }

  @Test
  public void exactlyTwoDaysResolvesToDayResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.plusDays(2);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.day);
  }

  @Test
  public void moreThanTwoDaysResolvesToDayResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.plusDays(2).plusMinutes(1);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.day);
  }

  @Test
  public void moreThanSixtyDaysResolvesToMonthResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.plusMonths(2);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.month);
  }

  @Test
  public void veryLongDurationResolvesToMonthResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.plusYears(10000);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.month);
  }

  @Test
  public void negativeDurationResolvesToHourResolution() {
    ZonedDateTime start = ZonedDateTime.parse("2018-03-26T02:10:45Z");
    ZonedDateTime end = start.minusDays(2);
    assertThat(defaultResolutionFor(Duration.between(start, end)))
      .isEqualTo(TemporalResolution.hour);
  }
}