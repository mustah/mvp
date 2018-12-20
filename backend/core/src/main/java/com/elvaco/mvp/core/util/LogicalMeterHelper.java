package com.elvaco.mvp.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;
import com.elvaco.mvp.core.exception.NoPhysicalMeters;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public final class LogicalMeterHelper {

  private final QuantityProvider quantityProvider;

  public Map<Quantity, List<PhysicalMeter>> groupByQuantity(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    if (logicalMeters.isEmpty() || quantities.isEmpty()) {
      return emptyMap();
    }

    Map<Quantity, List<PhysicalMeter>> physicalMeterQuantityMap = new HashMap<>();
    quantities.stream()
      .map(quantity -> {
        var storedQuantity = quantityProvider.getByName(quantity.name);
        if (storedQuantity == null) {
          return quantity;
        }
        return quantity.complementedBy(
          storedQuantity.getPresentationInformation(),
          storedQuantity.storageUnit
        );
      })
      .forEach(quantity -> {
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

  public Map<Quantity, List<PhysicalMeter>> mapMeterQuantitiesToPhysicalMeters(
    List<LogicalMeter> logicalMeters,
    Set<Quantity> quantities
  ) {
    if (logicalMeters.isEmpty()) {
      return emptyMap();
    }

    return quantities.stream()
      .map(complementQuantity(logicalMeters.get(0)))
      .flatMap(quantity -> logicalMeters.stream()
        .map(LogicalMeterHelper::getNoneEmptyPhysicalMeters)
        .map(physicalMeters -> new QuantityPhysicalMeters(quantity, physicalMeters)))
      .collect(groupingBy(
        QuantityPhysicalMeters::getQuantity,
        flatMapping(QuantityPhysicalMeters::getPhysicalMetersStream, toList())
      ));
  }

  private static Function<Quantity, Quantity> complementQuantity(LogicalMeter logicalMeter) {
    return quantity -> {
      Quantity lookedUpQuantity = logicalMeter.getQuantity(quantity.name)
        .orElseThrow(() -> new InvalidQuantityForMeterType(
          quantity.name,
          logicalMeter.meterDefinition.medium
        ));

      return quantity.complementedBy(
        lookedUpQuantity.getPresentationInformation(),
        lookedUpQuantity.storageUnit
      );
    };
  }

  private static List<PhysicalMeter> getNoneEmptyPhysicalMeters(LogicalMeter logicalMeter) {
    if (logicalMeter.physicalMeters.isEmpty()) {
      throw new NoPhysicalMeters(logicalMeter.id, logicalMeter.externalId);
    }
    return logicalMeter.physicalMeters;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static class QuantityPhysicalMeters {

    private final Quantity quantity;
    private final List<PhysicalMeter> physicalMeters;

    private Quantity getQuantity() {
      return quantity;
    }

    private Stream<PhysicalMeter> getPhysicalMetersStream() {
      return physicalMeters.stream();
    }
  }
}
