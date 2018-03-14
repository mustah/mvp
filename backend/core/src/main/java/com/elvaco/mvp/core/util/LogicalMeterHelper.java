package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogicalMeterHelper {

  public static Map<Quantity, List<UUID>> mapMeterQuantitiesToPhysicalMeterUuids(
    List<LogicalMeter> logicalMeters,
    List<Quantity> quantities
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


  private static List<UUID> getPhysicalMeterIds(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters
      .stream()
      .map(physicalMeter -> physicalMeter.id)
      .collect(toList());
  }


  public static double calculatedExpectedReadOuts(
    long readInterval,
    LocalDateTime after,
    LocalDateTime before
  ) {
    return Math.floor((double)
                        Duration.between(after, before).toMinutes() / readInterval
    );
  }
}
