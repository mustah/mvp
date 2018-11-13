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
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;
import com.elvaco.mvp.core.exception.NoPhysicalMeters;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyMap;

@UtilityClass
public final class LogicalMeterHelper {

  private static final int DAY_INTERVAL = 1440;
  private static final int HOUR_INTERVAL = 60;

  public static Map<Quantity, List<PhysicalMeter>> mapMeterQuantitiesToPhysicalMeters(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    if (logicalMeters.isEmpty()) {
      return emptyMap();
    }

    Map<Quantity, List<PhysicalMeter>> physicalMeterQuantityMap = new HashMap<>();
    quantities.forEach((quantity) -> {
      LogicalMeter firstMeter = logicalMeters.get(0);

      Quantity lookedUpQuantity = firstMeter.getQuantity(quantity.name)
        .orElseThrow(() -> new InvalidQuantityForMeterType(
          quantity.name,
          firstMeter.meterDefinition.medium
        ));

      Quantity complementedQuantity = quantity.complementedBy(
        lookedUpQuantity.getPresentationInformation(),
        lookedUpQuantity.storageUnit
      );

      List<PhysicalMeter> physicalMeters = new ArrayList<>();
      logicalMeters.forEach(logicalMeter -> {
          if (logicalMeter.physicalMeters.isEmpty()) {
            throw new NoPhysicalMeters(logicalMeter.id, logicalMeter.externalId);
          }
          physicalMeters.addAll(logicalMeter.physicalMeters);
        }
      );
      physicalMeterQuantityMap.put(complementedQuantity, physicalMeters);
    });
    return physicalMeterQuantityMap;
  }

  public static Map<Quantity, List<PhysicalMeter>> groupByQuantity(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    if (logicalMeters.isEmpty() || quantities.isEmpty()) {
      return emptyMap();
    }

    Map<Quantity, List<PhysicalMeter>> physicalMeterQuantityMap = new HashMap<>();
    quantities.forEach((quantity) -> {
      List<PhysicalMeter> physicalMeters = new ArrayList<>();
      logicalMeters.forEach(logicalMeter -> {
        if (logicalMeter.physicalMeters.isEmpty()) {
          throw new NoPhysicalMeters(logicalMeter.id, logicalMeter.externalId);
        }
        if (logicalMeter.getQuantity(quantity.name).isPresent()) {
          physicalMeters.addAll(logicalMeter.physicalMeters);
        }
      });

      if (!physicalMeters.isEmpty()) {
        physicalMeterQuantityMap.put(quantity, physicalMeters);
      }
    });
    return physicalMeterQuantityMap;
  }

  public static long calculateExpectedReadOuts(
    long readIntervalMinutes,
    SelectionPeriod selectionPeriod
  ) {
    if (readIntervalMinutes == 0) {
      return 0;
    }
    return (long) Math.floor((double) Duration.between(selectionPeriod.start, selectionPeriod.stop)
      .toMinutes() / readIntervalMinutes);
  }

  public static Function<LogicalMeterSummaryDto, LogicalMeterSummaryDto> withExpectedReadoutsFor(
    SelectionPeriod period
  ) {
    return logicalMeterSummaryDto -> logicalMeterSummaryDto.toBuilder()
      .expectedReadingCount(
        Optional.ofNullable(logicalMeterSummaryDto.readIntervalMinutes)
          .map(readInterval -> calculateExpectedReadOuts(readInterval, period))
          .orElse(0L))
      .build();
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
}
