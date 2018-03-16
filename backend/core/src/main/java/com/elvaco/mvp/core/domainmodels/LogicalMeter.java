package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
@Slf4j
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

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    ZonedDateTime created,
    List<PhysicalMeter> physicalMeters,
    MeterDefinition meterDefinition,
    List<Gateway> gateways,
    @Nullable Double collectionPercentage
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.location = location;
    this.created = ZonedDateTime.ofInstant(created.toInstant(), created.getZone());
    this.physicalMeters = unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
    this.gateways = unmodifiableList(gateways);
    this.collectionPercentage = collectionPercentage;
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
      null
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition
  ) {
    this(id, externalId, organisationId, meterDefinition, emptyList());
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    MeterDefinition meterDefinition,
    List<PhysicalMeter> physicalMeters
  ) {
    this(
      id,
      externalId,
      organisationId,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now(),
      physicalMeters,
      meterDefinition,
      emptyList(),
      null
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
      null
    );
  }

  @Override
  public UUID getId() {
    return id;
  }

  public LogicalMeter createdAt(ZonedDateTime creationTime) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      creationTime,
      physicalMeters,
      meterDefinition,
      gateways,
      collectionPercentage
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
      collectionPercentage
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
      collectionPercentage
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
      collectionPercentage
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
      .orElse("Unknown manufacturer");
  }

  public Optional<Quantity> getQuantity(String quantityName) {
    return getQuantities().stream()
      .filter(quantity -> quantity.name.equals(quantityName))
      .findAny();
  }

  private Optional<PhysicalMeter> activePhysicalMeter() {
    if (physicalMeters.size() == 1) {
      return Optional.of(physicalMeters.get(0));
    } else if (physicalMeters.isEmpty()) {
      return Optional.empty();
    }
    log.warn(
      "Active meter identification with multiple meters is not implemented! Returning last in "
        + "list (not necessarily correct).");
    return Optional.of(physicalMeters.get(physicalMeters.size() - 1));
  }
}
