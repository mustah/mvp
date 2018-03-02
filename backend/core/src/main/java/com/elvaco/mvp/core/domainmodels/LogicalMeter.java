package com.elvaco.mvp.core.domainmodels;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
  public final Date created;
  public final String externalId;
  public final UUID organisationId;
  public final MeterDefinition meterDefinition;
  public final List<MeterStatusLog> meterStatusLogs;
  public final List<Gateway> gateways;

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    Date created,
    List<PhysicalMeter> physicalMeters,
    MeterDefinition meterDefinition,
    List<MeterStatusLog> meterStatusLogs,
    List<Gateway> gateways
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.location = location;
    this.created = new Date(created.getTime());
    this.physicalMeters = unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
    this.meterStatusLogs = meterStatusLogs;
    this.gateways = unmodifiableList(gateways);
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
      new Date(),
      physicalMeters,
      meterDefinition,
      emptyList(),
      emptyList()
    );
  }

  public LogicalMeter(
    UUID id,
    String externalId,
    UUID organisationId,
    Location location,
    Date created
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
      emptyList()
    );
  }

  @Override
  public UUID getId() {
    return id;
  }

  public LogicalMeter createdAt(Date creationTime) {
    return new LogicalMeter(
      id,
      externalId,
      organisationId,
      location,
      creationTime,
      physicalMeters,
      meterDefinition,
      meterStatusLogs,
      gateways
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
      meterStatusLogs,
      gateways
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
      meterStatusLogs,
      singletonList(gateway)
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
      .orElse("Unknown manufacturer");
  }

  private Optional<PhysicalMeter> activePhysicalMeter() {
    if (physicalMeters.size() == 1) {
      return Optional.of(physicalMeters.get(0));
    } else if (physicalMeters.isEmpty()) {
      return Optional.empty();
    }
    throw new UnsupportedOperationException(
      "Active meter identification with multiple meters is not implemented!");
  }
}
