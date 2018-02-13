package com.elvaco.mvp.web.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class Dates {
  private Dates() {
  }

  public static String formatTime(Date time, TimeZone timeZone) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(timeZone);
    return dateFormat.format(time);
  }
}
