package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(doNotUseGetters = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogicalMeter implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -7467562865583613538L;

  @Builder.Default
  public UUID id = UUID.randomUUID();
  public final String externalId;
  public final UUID organisationId;
  @Builder.Default
  public MeterDefinition meterDefinition = MeterDefinition.UNKNOWN_METER;
  @Builder.Default
  public ZonedDateTime created = ZonedDateTime.now();
  @Singular
  public final List<PhysicalMeter> physicalMeters;
  @Singular
  public final List<Gateway> gateways;
  @Builder.Default
  public Location location = Location.UNKNOWN_LOCATION;
  @Nullable
  public final Long expectedMeasurementCount;
  @Nullable
  public final Long missingMeasurementCount;
  @Nullable
  public final AlarmLogEntry alarm;
  @Nullable
  public final StatusType status;

  @Override
  public UUID getId() {
    return id;
  }

  public StatusType currentStatus() {
    return activeStatusLog()
      .map(statusLogEntry -> statusLogEntry.status)
      .orElse(StatusType.UNKNOWN);
  }

  public Optional<StatusLogEntry<UUID>> activeStatusLog() {
    return physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .filter(StatusLogEntry::isActive)
      .max(Comparator.comparing(o -> o.start));
  }

  public LogicalMeter addPhysicalMeter(PhysicalMeter physicalMeter) {
    List<PhysicalMeter> newPhysicalMeters = new ArrayList<>(physicalMeters).stream()
      .filter(meter -> meter.id.equals(physicalMeter.id))
      .collect(toList());
    newPhysicalMeters.add(physicalMeter);

    return toBuilder().physicalMeters(newPhysicalMeters).build();
  }

  @Nullable
  public Double getCollectionPercentage() {
    Double collectionPercentage = getCollectionStats().collectionPercentage;
    return collectionPercentage.isNaN() ? null : collectionPercentage;
  }

  public CollectionStats getCollectionStats() {
    return new CollectionStats(
      missingMeasurementCount == null ? 0L : missingMeasurementCount,
      expectedMeasurementCount == null ? 0L : expectedMeasurementCount
    );
  }

  public String getMedium() {
    return meterDefinition.medium;
  }

  public Set<Quantity> getQuantities() {
    return meterDefinition != null ? meterDefinition.quantities : emptySet();
  }

  public String getManufacturer() {
    return activePhysicalMeter()
      .map(physicalMeter -> physicalMeter.manufacturer)
      .orElse("UNKNOWN");
  }

  public Optional<Quantity> getQuantity(String quantityName) {
    return getQuantities().stream()
      .filter(quantity -> quantity.name.equals(quantityName))
      .findAny();
  }

  public Optional<PhysicalMeter> activePhysicalMeter() {
    if (physicalMeters.size() == 1) {
      return Optional.of(physicalMeters.get(0));
    } else if (physicalMeters.isEmpty()) {
      return Optional.empty();
    }
    //TODO: Implement actual active meter identification
    return Optional.of(physicalMeters.get(physicalMeters.size() - 1));
  }
}
