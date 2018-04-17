package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Dates {

  public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(UTC.toZoneId());

  public static String formatUtc(ZonedDateTime time) {
    return UTC_DATE_TIME_FORMATTER.format(time);
  }
}
