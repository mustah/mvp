package com.elvaco.mvp.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class Dates {
  public static LocalDateTime parseDateTime(String s) {
    //TODO get correct time zone
    return LocalDateTime.ofInstant(Instant.parse(s), TimeZone.getTimeZone("UTC").toZoneId());
  }
}
