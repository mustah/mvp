package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

public class Dates {
  public static ZonedDateTime parseDateTime(String s) {
    //TODO get correct time zone
    return ZonedDateTime.parse(s);
  }

  public static ZonedDateTime of(Date date) {
    //TODO get correct time zone
    return ZonedDateTime.ofInstant(date.toInstant(), TimeZone.getTimeZone("UTC").toZoneId());
  }
}
