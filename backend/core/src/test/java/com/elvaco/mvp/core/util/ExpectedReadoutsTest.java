package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;

import org.junit.Test;

import static com.elvaco.mvp.core.util.ExpectedReadouts.expectedReadouts;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpectedReadoutsTest {

  @Test
  public void expectedReadouts_interval_60_during_day() {
    assertThat(expectedReadouts(
      60,
      new FilterPeriod(
        ZonedDateTime.parse("2018-01-01T00:00:00Z"),
        ZonedDateTime.parse("2018-01-02T00:00:00Z")
      )
    ))
      .isEqualTo(24);
  }

  @Test
  public void expectedReadouts_interval_15_during_day() {
    assertThat(expectedReadouts(
      15,
      new FilterPeriod(
        ZonedDateTime.parse("2018-01-01T00:00:00Z"),
        ZonedDateTime.parse("2018-01-02T00:00:00Z")
      )
    ))
      .isEqualTo(96);
  }

  @Test
  public void expectedReadouts_interval_15_during_hour() {
    assertThat(expectedReadouts(
      15,
      new FilterPeriod(
        ZonedDateTime.parse("2001-01-01T13:00:00Z"),
        ZonedDateTime.parse("2001-01-01T14:00:00Z")
      )
    ))
      .isEqualTo(4);
  }

  @Test
  public void expectedReadouts_interval_0_during_day() {
    assertThat(expectedReadouts(
      0,
      new FilterPeriod(
        ZonedDateTime.parse("2018-01-01T00:00:00Z"),
        ZonedDateTime.parse("2018-01-02T00:00:00Z")
      )
    ))
      .isEqualTo(0);
  }
}
