package com.elvaco.mvp.core.util;

import java.time.Duration;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExpectedReadouts {

  public static long expectedReadouts(
    long readIntervalMinutes,
    SelectionPeriod selectionPeriod
  ) {
    if (readIntervalMinutes == 0) {
      return 0;
    }
    return (long) Math.floor(
      (double)
        Duration.between(selectionPeriod.start, selectionPeriod.stop).toMinutes()
        /
        readIntervalMinutes
    );
  }
}
