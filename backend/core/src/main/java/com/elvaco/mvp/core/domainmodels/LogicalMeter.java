package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
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

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(doNotUseGetters = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogicalMeter implements Identifiable<UUID> {

  public static final String UTC_OFFSET = "+01";

  public final String externalId;
  public final UUID organisationId;
  @Singular
  public final List<PhysicalMeter> physicalMeters;
  @Singular
  public final List<Gateway> gateways;
  @Singular
  public final List<AlarmLogEntry> alarms;
  @Nullable
  public final StatusType status;
  @Builder.Default
  public String utcOffset = UTC_OFFSET;
  @Builder.Default
  public UUID id = UUID.randomUUID();
  @Builder.Default
  public MeterDefinition meterDefinition = MeterDefinition.UNKNOWN;
  @Builder.Default
  public ZonedDateTime created = ZonedDateTime.now();
  @Builder.Default
  public Location location = Location.UNKNOWN_LOCATION;

  @Override
  public UUID getId() {
    return id;
  }

  public StatusType currentStatus() {
    return activeStatusLog()
      .map(statusLogEntry -> statusLogEntry.status)
      .orElse(StatusType.UNKNOWN);
  }

  public Optional<StatusLogEntry> activeStatusLog() {
    return physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .filter(StatusLogEntry::isActive)
      .max(Comparator.comparing(o -> o.start));
  }

  public MeterDefinition getMeterDefinition() {
    return meterDefinition;
  }

  public Medium getMedium() {
    return meterDefinition.medium;
  }

  public String getManufacturer() {
    return activePhysicalMeter()
      .map(physicalMeter -> physicalMeter.manufacturer)
      .orElse("UNKNOWN");
  }

  public Optional<PhysicalMeter> activePhysicalMeter() {
    return activePhysicalMeter(ZonedDateTime.now());
  }

  public Optional<PhysicalMeter> activePhysicalMeter(ZonedDateTime when) {
    return physicalMeters.stream().filter(pm -> pm.activePeriod.contains(when)).findAny();
  }

  public Set<DisplayQuantity> getQuantities() {
    return meterDefinition.quantities;
  }

  public Optional<Quantity> getQuantity(String name) {
    return meterDefinition.quantities.stream()
      .filter(displayQuantity -> displayQuantity.quantity.name.equals(name))
      .map(displayQuantity -> displayQuantity.quantity)
      .findAny();
  }
}
