package com.elvaco.mvp.core.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
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
    if (logicalMeters.isEmpty()) {
      return emptyMap();
    }

    return quantities.stream()
      .map(this::resolveQuantity)
      .flatMap(quantity -> logicalMeters.stream()
        .filter(logicalMeter -> logicalMeter.getQuantity(quantity.name).isPresent())
        .map(LogicalMeterHelper::getNoneEmptyPhysicalMeters)
        .map(physicalMeters -> new QuantityPhysicalMeters(quantity, physicalMeters)))
      .collect(groupingBy(
        QuantityPhysicalMeters::getQuantity,
        flatMapping(QuantityPhysicalMeters::getPhysicalMetersStream, toList())
      ));
  }

  public Quantity resolveQuantity(Quantity quantity) {
    return Optional.ofNullable(quantityProvider.getByName(quantity.name))
      .map(storedQuantity -> quantity.complementedBy(
        storedQuantity.getPresentationInformation(),
        storedQuantity.storageUnit
      ))
      .orElse(quantity);
  }

  private static List<PhysicalMeter> getNoneEmptyPhysicalMeters(LogicalMeter logicalMeter) {
    return Optional.of(logicalMeter.physicalMeters)
      .filter(CollectionHelper::isNotEmpty)
      .orElseThrow(() -> new NoPhysicalMeters(logicalMeter.id, logicalMeter.externalId));
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
