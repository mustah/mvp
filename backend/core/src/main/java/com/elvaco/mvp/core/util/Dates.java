package com.elvaco.mvp.core.util;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.TimeZone;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Dates {

  public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      .withZone(UTC.toZoneId());

  public static String formatUtc(ZonedDateTime time) {
    return UTC_DATE_TIME_FORMATTER.format(time);
  }

  public static ZonedDateTime epoch() {
    return ZonedDateTime.ofInstant(Instant.EPOCH, UTC.toZoneId());
  }

  public static ZonedDateTime earliest(Collection<ZonedDateTime> dateTimes) {
    return dateTimes.stream()
      .min(ZonedDateTime::compareTo)
      .orElseThrow(() -> new IllegalArgumentException("Empty collection"));
  }

  public static ZonedDateTime latest(Collection<ZonedDateTime> dateTimes) {
    return dateTimes.stream()
      .max(ZonedDateTime::compareTo)
      .orElseThrow(() -> new IllegalArgumentException("Empty collection"));
  }
}
