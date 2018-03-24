package com.elvaco.mvp.web.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public final class Dates {

  private Dates() {}

  public static String formatTime(ZonedDateTime time, TimeZone timeZone) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(timeZone.toZoneId());

    return dateTimeFormatter.format(time);
  }
}
