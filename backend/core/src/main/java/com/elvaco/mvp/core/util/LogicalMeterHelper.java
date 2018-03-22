package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogicalMeterHelper {

  private static final int DAY_INTERVAL = 1440;
  private static final int HOUR_INTERVAL = 60;

  public static Map<Quantity, List<UUID>> mapMeterQuantitiesToPhysicalMeterUuids(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    Map<Quantity, List<UUID>> physicalMeterQuantityMap = new HashMap<>();
    quantities.forEach((quantity) -> {
      List<UUID> physicalMeterIds = new ArrayList<>();
      for (LogicalMeter meter : logicalMeters) {
        Optional<Quantity> meterQuantity = meter.getQuantity(quantity.name);
        if (meterQuantity.isPresent()) {
          if (!quantity.unit().isPresent() && meterQuantity.get().unit().isPresent()) {
            quantity = quantity.withUnit(meterQuantity.get().unit);
          }
          physicalMeterIds.addAll(getPhysicalMeterIds(meter));
        }
      }
      physicalMeterQuantityMap.put(quantity, physicalMeterIds);
    });
    return physicalMeterQuantityMap;
  }

  public static Double calculateExpectedReadOuts(
    PhysicalMeter physicalMeter,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    return physicalMeter.statuses
      .stream()
      .filter(status -> StatusType.ACTIVE == StatusType.from(status.name))
      .mapToDouble(status -> {
        ZonedDateTime startPoint = getStartPoint(
          status.start,
          after,
          physicalMeter.readIntervalMinutes
        );

        ZonedDateTime endPoint = getEndPoint(
          status.stop == null ? before : status.stop,
          before
        );

        return calculateExpectedReadOuts(
          physicalMeter.readIntervalMinutes,
          startPoint,
          endPoint
        );
      })
      .reduce(0.0, (d1, d2) -> d1 + d2);
  }

  public static double calculateExpectedReadOuts(
    long readIntervalMinutes,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    return Math.floor((double) Duration.between(after, before).toMinutes() / readIntervalMinutes);
  }

  /**
   * Get next anticipated read of a meter.
   *
   * @param date     Date to start from
   * @param interval Read interval for the meter
   *
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

  /**
   * Decides whether to use status start date or period as starting point.
   */
  private static ZonedDateTime getStartPoint(
    ZonedDateTime statusStart,
    ZonedDateTime periodAfter,
    Long readIntervalMinutes
  ) {
    return getFirstDateMatchingInterval(
      statusStart.isAfter(periodAfter) ? statusStart : periodAfter,
      readIntervalMinutes
    );
  }

  /**
   * Decides whether to use status end date or period as end point.
   */
  private static ZonedDateTime getEndPoint(ZonedDateTime statusEnd, ZonedDateTime periodBefore) {
    return statusEnd.isBefore(periodBefore) ? statusEnd : periodBefore;
  }
}
