package com.elvaco.mvp.web.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public final class Dates {
  private Dates() {
  }

  //TODO remove
  public static String formatTime(Date time, TimeZone timeZone) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(timeZone);
    return dateFormat.format(time);
  }

  public static String formatTime(ZonedDateTime time, TimeZone timeZone) {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(time);
  }
}
