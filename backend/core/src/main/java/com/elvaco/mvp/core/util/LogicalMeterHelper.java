package com.elvaco.mvp.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.InvalidQuantityForMeterType;
import com.elvaco.mvp.core.exception.NoPhysicalMeters;

import lombok.RequiredArgsConstructor;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

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
    var lookedUpQuantities = quantities.stream()
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
      .collect(toSet());

    lookedUpQuantities.forEach((quantity) -> {
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
}
