package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.TemporalResolution.defaultResolutionFor;
import static org.assertj.core.api.Assertions.assertThat;

public class TemporalResolutionTest {

  private static final ZonedDateTime DATE = ZonedDateTime.parse("2018-10-04T21:47:15.34Z");
  private static final ZonedDateTime START = ZonedDateTime.parse("2018-03-26T02:10:45Z");

  @Test
  public void intervalStartForDayInterval() {
    OffsetDateTime startInterval = TemporalResolution.day.getStart(DATE);
    assertThat(startInterval).isEqualTo(ZonedDateTime.parse("2018-10-04T00:00:00.000Z")
      .toOffsetDateTime());
  }

  @Test
  public void intervalStartForHourInterval() {
    OffsetDateTime startInterval = TemporalResolution.hour.getStart(DATE);
    assertThat(startInterval).isEqualTo(ZonedDateTime.parse("2018-10-04T21:00:00.000Z")
      .toOffsetDateTime());
  }

  @Test
  public void intervalStartForMonthInterval() {
    OffsetDateTime startInterval = TemporalResolution.month.getStart(DATE);
    assertThat(startInterval).isEqualTo(ZonedDateTime.parse("2018-10-01T00:00:00.000Z")
      .toOffsetDateTime());
  }

  @Test
  public void lessThanADayResolvesToHourResolution() {
    ZonedDateTime end = START.plusHours(23);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.hour);
  }

  @Test
  public void exactlyTwoDaysResolvesToDayResolution() {
    ZonedDateTime end = START.plusDays(2);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.day);
  }

  @Test
  public void moreThanTwoDaysResolvesToDayResolution() {
    ZonedDateTime end = START.plusDays(2).plusMinutes(1);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.day);
  }

  @Test
  public void moreThanSixtyDaysResolvesToMonthResolution() {
    ZonedDateTime end = START.plusMonths(2);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.month);
  }

  @Test
  public void veryLongDurationResolvesToMonthResolution() {
    ZonedDateTime end = START.plusYears(10000);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.month);
  }

  @Test
  public void negativeDurationResolvesToHourResolution() {
    ZonedDateTime end = START.minusDays(2);
    assertThat(defaultResolutionFor(Duration.between(START, end)))
      .isEqualTo(TemporalResolution.hour);
  }
}
