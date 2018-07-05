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

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public class LogicalMeter implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -7467562865583613538L;

  public final UUID id;
  public final Location location;
  public final List<PhysicalMeter> physicalMeters;
  public final ZonedDateTime created;
  public final String externalId;
  public final UUID organisationId;
  public final MeterDefinition meterDefinition;
  public final List<Gateway> gateways;
  public final List<Measurement> latestReadouts;
  @Nullable
  public final StatusLogEntry<UUID> currentStatus;
  @Nullable
  public final Long expectedMeasurementCount;
  @Nullable
  public final Long actualMeasurementCount;

  public LogicalMeter(
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
    @Nullable Long actualMeasurementCount,
    @Nullable StatusLogEntry<UUID> status
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.location = location;
    this.created = created;
    this.physicalMeters = unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
    this.gateways = unmodifiableList(gateways);
    this.latestReadouts = latestReadouts;
    this.expectedMeasurementCount = expectedMeasurementCount;
    this.actualMeasurementCount = actualMeasurementCount;
    this.currentStatus = status;
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    ZonedDateTime created,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways,
    Location location
  ) {
    this(
      id,
      externalId,
      organisationId,
      meterDefinition,
      created,
      physicalMeters,
      gateways,
      emptyList(),
      location,
      null,
      null,
      null
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    Location location
  ) {
    this(
      id,
      externalId,
      organisationId,
      meterDefinition,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      emptyList(),
      location,
      null,
      null,
      null
    );
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

  LogicalMeter createdAt(ZonedDateTime creationTime) {
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
      actualMeasurementCount,
      currentStatus
    );
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
      actualMeasurementCount,
      currentStatus
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
      actualMeasurementCount,
      currentStatus
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
      actualMeasurementCount,
      currentStatus
    );
  }

  public LogicalMeter withMeasurements(
    List<Measurement> measurements
  ) {
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
      actualMeasurementCount,
      currentStatus
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
      actualMeasurementCount,
      currentStatus
    );
  }

  public Optional<Double> getCollectionPercentage() {
    Double collectionPercentage = getCollectionStats().getCollectionPercentage();

    if (collectionPercentage.isNaN()) {
      return Optional.empty();
    }

    return Optional.of(collectionPercentage);
  }

  public CollectionStats getCollectionStats() {

    return new CollectionStats(
      actualMeasurementCount == null ? 0L : actualMeasurementCount,
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
