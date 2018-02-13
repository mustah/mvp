package com.elvaco.mvp.web.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatesTest {

  @Test
  public void formatTime() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));

    // 1990-04-09 08:45:12
    calendar.set(1990, 3, 9, 8, 45, 12);
    Date date = calendar.getTime();
    String actual = Dates.formatTime(date, TimeZone.getTimeZone("Europe/Stockholm"));
    assertThat(actual).isEqualTo("1990-04-09 08:45:12");
    date.setTime(date.getTime() + 3600 * 1000);
    actual = Dates.formatTime(date, TimeZone.getTimeZone("Europe/Stockholm"));
    assertThat(actual).isEqualTo("1990-04-09 09:45:12");

    actual = Dates.formatTime(date, TimeZone.getTimeZone("Pacific/Tongatapu"));
    assertThat(actual).isEqualTo("1990-04-09 20:45:12");
  }
}
