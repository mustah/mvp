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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

@Builder
@ToString
@EqualsAndHashCode(doNotUseGetters = true)
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
  @Singular
  public final List<Measurement> latestReadouts;
  @Builder.Default
  public Location location = Location.UNKNOWN_LOCATION;
  @Nullable
  public final Long expectedMeasurementCount;
  @Nullable
  public final Long missingMeasurementCount;
  @Nullable
  public final StatusLogEntry<UUID> currentStatus;
  @Nullable
  public final AlarmLogEntry alarm;

  private LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    ZonedDateTime created,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways,
    List<Measurement> latestReadouts,
    Location location,
    @Nullable Long expectedMeasurementCount,
    @Nullable Long missingMeasurementCount,
    @Nullable StatusLogEntry<UUID> currentStatus,
    @Nullable AlarmLogEntry alarm
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.meterDefinition = meterDefinition;
    this.created = created;
    this.physicalMeters = unmodifiableList(physicalMeters);
    this.gateways = unmodifiableList(gateways);
    this.latestReadouts = unmodifiableList(latestReadouts);
    this.location = location;
    this.expectedMeasurementCount = expectedMeasurementCount;
    this.missingMeasurementCount = missingMeasurementCount;
    this.currentStatus = currentStatus;
    this.alarm = alarm;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public StatusType currentStatus() {
    if (currentStatus != null) {
      return currentStatus.status;
    }

    return physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .filter(StatusLogEntry::isActive)
      .max(Comparator.comparing(o -> o.start))
      .map(statusLogEntry -> statusLogEntry.status)
      .orElse(StatusType.UNKNOWN);
  }

  public LogicalMeter withMeterDefinition(MeterDefinition meterDefinition) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      physicalMeters,
      gateways,
      latestReadouts,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
  }

  public LogicalMeter withPhysicalMeter(PhysicalMeter physicalMeter) {
    List<PhysicalMeter> newPhysicalMeters = new ArrayList<>(physicalMeters);
    newPhysicalMeters.removeIf(physicalMeter1 -> physicalMeter1.id.equals(physicalMeter.id));
    newPhysicalMeters.add(physicalMeter);

    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      newPhysicalMeters,
      gateways,
      latestReadouts,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
  }

  public LogicalMeter withGateway(Gateway gateway) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      physicalMeters,
      singletonList(gateway),
      latestReadouts,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
  }

  public LogicalMeter withMeasurements(List<Measurement> measurements) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      physicalMeters,
      gateways,
      measurements,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
  }

  public LogicalMeter withLocation(Location location) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      physicalMeters,
      gateways,
      latestReadouts,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
  }

  public LogicalMeter createdAt(ZonedDateTime creationTime) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      meterDefinition,
      creationTime,
      physicalMeters,
      gateways,
      latestReadouts,
      location,
      expectedMeasurementCount,
      missingMeasurementCount,
      currentStatus,
      alarm
    );
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
