package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
public class LogicalMeter implements Identifiable<UUID> {

  public final UUID id;
  public final Location location;
  public final List<PhysicalMeter> physicalMeters;
  public final ZonedDateTime created;
  public final String externalId;
  public final UUID organisationId;
  public final MeterDefinition meterDefinition;
  public final List<Gateway> gateways;
  public final Double collectionPercentage;
  public final Long readIntervalMinutes;
  public final List<Measurement> measurements;

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    ZonedDateTime created,
    List<PhysicalMeter> physicalMeters,
    MeterDefinition meterDefinition,
    List<Gateway> gateways,
    @Nullable Double collectionPercentage,
    List<Measurement> measurements
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.location = location;
    this.created = created;
    this.physicalMeters = unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
    this.gateways = unmodifiableList(gateways);

    if (collectionPercentage != null
        && (collectionPercentage < 0.0 || collectionPercentage > 100.0)) {
      throw new IllegalArgumentException(String.format(
        "Collection percentage must be >= 0 and <= 100, but was: %s for logical meter '%s'",
        collectionPercentage,
        id
      ));
    }

    this.collectionPercentage = Optional.ofNullable(collectionPercentage)
      .filter(percentage -> !percentage.isNaN())
      .orElse(null);
    this.readIntervalMinutes = activePhysicalMeter()
      .map(pm -> pm.readIntervalMinutes)
      .orElse(null);
    this.measurements = measurements;
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    ZonedDateTime created,
    List<PhysicalMeter> physicalMeters,
    MeterDefinition meterDefinition,
    List<Gateway> gateways
  ) {
    this(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      gateways,
      null,
      emptyList()
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    Location location
  ) {
    this(id, externalId, organisationId, meterDefinition, location, emptyList());
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    Location location,
    List<PhysicalMeter> physicalMeters
  ) {
    this(
      id,
      externalId,
      organisationId,
      location,
      ZonedDateTime.now(),
      physicalMeters,
      meterDefinition,
      emptyList(),
      null,
      emptyList()
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    ZonedDateTime created
  ) {
    this(
      id,
      externalId,
      organisationId,
      location,
      created,
      emptyList(),
      MeterDefinition.UNKNOWN_METER,
      emptyList(),
      null,
      emptyList()
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    List<Gateway> gateways,
    ZonedDateTime created
  ) {
    this(
      id,
      externalId,
      organisationId,
      location,
      created,
      emptyList(),
      MeterDefinition.UNKNOWN_METER,
      gateways,
      null,
      emptyList()
    );
  }

  @Override
  public UUID getId() {
    return id;
  }

  LogicalMeter createdAt(ZonedDateTime creationTime) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      creationTime,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
    );
  }

  public LogicalMeter withMeterDefinition(MeterDefinition meterDefinition) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
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
      location,
      created,
      newPhysicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
    );
  }

  public LogicalMeter withGateway(Gateway gateway) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      singletonList(gateway),
      collectionPercentage,
      measurements
    );
  }

  public LogicalMeter withCollectionPercentage(
    @Nullable Double collectionPercentage
  ) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
    );
  }

  public LogicalMeter withMeasurements(
    List<Measurement> measurements
  ) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
    );
  }

  public LogicalMeter withLocation(Location location) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      created,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage,
      measurements
    );
  }

  public Optional<Double> getCollectionPercentage() {
    return Optional.ofNullable(collectionPercentage);
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
