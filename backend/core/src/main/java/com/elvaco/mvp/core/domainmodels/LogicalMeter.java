package com.elvaco.mvp.core.domainmodels;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public class LogicalMeter {

  @Nullable
  public final Long id;
  public final Location location;
  public final List<PhysicalMeter> physicalMeters;
  public final Date created;
  public final String externalId;
  public final Long organisationId;

  @Nullable
  public final MeterDefinition meterDefinition;
  public final List<MeterStatusLog> meterStatusLogs;

  public LogicalMeter(
    String externalId,
    Long organisationId,
    @Nullable MeterDefinition meterDefinition
  ) {
    this(
      externalId, organisationId, meterDefinition,
      Collections.emptyList()
    );
  }

  public LogicalMeter(
    String externalId, Long organisationId, @Nullable MeterDefinition meterDefinition,
    List<PhysicalMeter> physicalMeters
  ) {
    this(
      null,
      externalId, organisationId, Location.UNKNOWN_LOCATION,
      new Date(),
      physicalMeters,
      meterDefinition,
      Collections.emptyList()
    );
  }

  public LogicalMeter(
    @Nullable Long id,
    String externalId,
    Long organisationId,
    Location location,
    Date created
  ) {
    this(
      id,
      externalId,
      organisationId,
      location,
      created,
      Collections.emptyList(),
      null,
      Collections.emptyList()
    );
  }

  public LogicalMeter(
    @Nullable Long id,
    String externalId, Long organisationId, Location location,
    Date created,
    List<PhysicalMeter> physicalMeters,
    @Nullable MeterDefinition meterDefinition,
    List<MeterStatusLog> meterStatusLogs
  ) {
    this.id = id;
    this.externalId = externalId;
    this.organisationId = organisationId;
    this.location = location;
    this.created = new Date(created.getTime());
    this.physicalMeters = Collections.unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
    this.meterStatusLogs = meterStatusLogs;
  }

  public LogicalMeter createdAt(Date creationTime) {
    return new LogicalMeter(
      id,
      externalId, organisationId, location,
      creationTime,
      physicalMeters,
      meterDefinition,
      Collections.emptyList()
    );
  }

  public String getMedium() {
    return meterDefinition != null ? meterDefinition.medium : "Unknown medium";
  }

  public Set<Quantity> getQuantities() {
    return meterDefinition != null ? meterDefinition.quantities : Collections.emptySet();
  }

  public boolean hasMeterDefinition() {
    return meterDefinition != null;
  }

  public String getManufacturer() {
    return activePhysicalMeter()
      .map(physicalMeter -> physicalMeter.manufacturer)
      .orElse("Unknown manufacturer");
  }

  public LogicalMeter withMeterDefinition(MeterDefinition meterDefinition) {
    return new LogicalMeter(
      id,
      externalId, organisationId, location,
      created,
      physicalMeters,
      meterDefinition,
      Collections.emptyList()
    );
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
