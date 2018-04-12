package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.stream.Collectors.toList;

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
          continue;
        }
        Quantity meterQuantity = meter.getQuantity(quantity.name).get();
        if (quantity.defaultPresentationUnit() == null
          && meterQuantity.defaultPresentationUnit() != null) {
          quantity = quantity.withUnit(meterQuantity.defaultPresentationUnit());
        }
        if (quantity.defaultSeriesDisplayMode().equals(SeriesDisplayMode.UNKNOWN)) {
          quantity = quantity.withSeriesDisplayMode(meterQuantity.defaultSeriesDisplayMode());
        }
        physicalMeters.addAll(meter.physicalMeters);
      }
      physicalMeterQuantityMap.put(quantity, physicalMeters);
    });
    return physicalMeterQuantityMap;
  }

  public static Double calculateExpectedReadOuts(
    PhysicalMeter physicalMeter,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    return calculateExpectedReadOuts(
      physicalMeter.readIntervalMinutes,
      after,
      before
    );
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

  /**
   * Get next anticipated read of a meter.
   *
   * @param date     Date to start from
   * @param interval Read interval for the meter
   * @return
   */
  public static ZonedDateTime getFirstDateMatchingInterval(ZonedDateTime date, Long interval) {
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

  private static List<UUID> getPhysicalMeterIds(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters
      .stream()
      .map(physicalMeter -> physicalMeter.id)
      .collect(toList());
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
      expectedReadouts += calculateExpectedReadOuts(physicalMeter, after, before);
      actualReadouts += physicalMeter.getMeasurementCountOrZero();
    }
    return new CollectionStats(actualReadouts, expectedReadouts * expectedQuantityCount);
  }
}
