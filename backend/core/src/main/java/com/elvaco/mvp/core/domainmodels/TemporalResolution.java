package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;

public enum TemporalResolution {
  hour,
  day,
  month;

  public static TemporalResolution defaultResolutionFor(Duration duration) {
    if (duration.toDays() < 2) {
      return hour;
    } else if (duration.toDays() < 60) {
      return day;
    } else {
      return month;
    }
  }
}

