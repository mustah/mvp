package com.elvaco.mvp.core.util;

import java.time.Duration;

import com.elvaco.mvp.core.domainmodels.TemporalResolution;

public final class ResolutionHelper {

  public static TemporalResolution defaultResolutionFor(Duration duration) {
    if (duration.toDays() < 2) {
      return TemporalResolution.hour;
    } else if (duration.minusDays(30).isNegative()) {
      return TemporalResolution.day;
    } else {
      return TemporalResolution.month;
    }
  }
}
