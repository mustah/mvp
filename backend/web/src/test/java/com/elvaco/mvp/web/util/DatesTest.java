package com.elvaco.mvp.web.util;

import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatesTest {

  @Test
  public void formatTime2() {
    // 1990-04-09 08:45:12
    ZonedDateTime date = ZonedDateTime.of(
      1990,
      4,
      9,
      8,
      45,
      12,
      0,
      TimeZone.getTimeZone("Europe/Stockholm").toZoneId()
    );

    String actual = Dates.formatTime(date, TimeZone.getTimeZone("Europe/Stockholm"));
    assertThat(actual).isEqualTo("1990-04-09 08:45:12");

    ZonedDateTime date2 = date.plusHours(1);
    actual = Dates.formatTime(date2, TimeZone.getTimeZone("Europe/Stockholm"));
    assertThat(actual).isEqualTo("1990-04-09 09:45:12");

    actual = Dates.formatTime(date2, TimeZone.getTimeZone("Pacific/Tongatapu"));
    assertThat(actual).isEqualTo("1990-04-09 20:45:12");
  }
}
