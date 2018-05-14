package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogicalMeterHelper {

  private static final int DAY_INTERVAL = 1440;
  private static final int HOUR_INTERVAL = 60;

  public static Map<Quantity, List<PhysicalMeter>> mapMeterQuantitiesToPhysicalMeters(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    Map<Quantity, List<PhysicalMeter>> physicalMeterQuantityMap = new HashMap<>();
    quantities.forEach((quantity) -> {
      List<PhysicalMeter> physicalMeters = new ArrayList<>();
      for (LogicalMeter meter : logicalMeters) {
        if (!meter.getQuantity(quantity.name).isPresent()) {
          throw new InvalidQuantityForMeterType(quantity.name, meter.meterDefinition.medium);
        }
        Quantity meterQuantity = meter.getQuantity(quantity.name).get();
        if (quantity.presentationUnit() == null && meterQuantity.presentationUnit() != null) {
          quantity = quantity.withUnit(meterQuantity.presentationUnit());
        }
        if (quantity.seriesDisplayMode().equals(SeriesDisplayMode.UNKNOWN)) {
          quantity = quantity.withSeriesDisplayMode(meterQuantity.seriesDisplayMode());
        }
        physicalMeters.addAll(meter.physicalMeters);
      }
      physicalMeterQuantityMap.put(quantity, physicalMeters);
    });
    return physicalMeterQuantityMap;
  }

  public static double calculateExpectedReadOuts(
    long readIntervalMinutes,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    if (readIntervalMinutes == 0) {
      return 0;
    }
    return Math.floor((double) Duration.between(after, before).toMinutes() / readIntervalMinutes);
  }

  public static ZonedDateTime getNextReadoutDate(ZonedDateTime date, Long interval) {
    if (interval == DAY_INTERVAL) {
      if (date.getHour() == 0 && date.getMinute() == 0) {
        return date;
      }
      return date.truncatedTo(ChronoUnit.DAYS).plusDays(1);
    }

    if (interval <= HOUR_INTERVAL) {
      if (date.getMinute() == 0) {
        return ZonedDateTime.ofInstant(date.toInstant(), date.getZone());
      }
      return date.truncatedTo(ChronoUnit.HOURS)
        .plusMinutes(interval * (date.getMinute() / interval) + interval);
    }

    throw new RuntimeException("Unhandled meter interval");
  }

  public static CollectionStats getCollectionPercent(
    List<PhysicalMeter> physicalMeters,
    ZonedDateTime after,
    ZonedDateTime before,
    int expectedQuantityCount
  ) {
    double expectedReadouts = 0.0;
    double actualReadouts = 0.0;

    for (PhysicalMeter physicalMeter : physicalMeters) {
      expectedReadouts += calculateExpectedReadOuts(
        physicalMeter.readIntervalMinutes,
        after,
        before
      );
      actualReadouts += physicalMeter.getMeasurementCountOrZero();
    }
    return new CollectionStats(actualReadouts, expectedReadouts * expectedQuantityCount);
  }
}
