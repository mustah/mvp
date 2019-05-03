package com.elvaco.mvp.core.util;

import java.time.Duration;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExpectedReadouts {

  public static long expectedReadouts(
    long readIntervalMinutes,
    FilterPeriod filterPeriod
  ) {
    if (readIntervalMinutes == 0) {
      return 0;
    }
    return (long) Math.floor(
      (double)
        Duration.between(filterPeriod.start, filterPeriod.stop).toMinutes()
        /
        readIntervalMinutes
    );
  }
}
