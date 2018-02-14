package com.elvaco.mvp.core.domainmodels;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public class LogicalMeter {

  @Nullable
  public final Long id;
  public final String status;
  public final Location location;
  public final PropertyCollection propertyCollection;
  public final List<PhysicalMeter> physicalMeters;
  public final Date created;
  @Nullable
  public final MeterDefinition meterDefinition;

  public LogicalMeter(@Nullable MeterDefinition meterDefinition) {
    this(
      meterDefinition,
      Collections.emptyList()
    );
  }

  public LogicalMeter(
    @Nullable MeterDefinition meterDefinition,
    List<PhysicalMeter> physicalMeters
  ) {
    this(
      null,
      "Ok",
      Location.UNKNOWN_LOCATION,
      new Date(),
      PropertyCollection.empty(),
      physicalMeters,
      meterDefinition
    );
  }

  public LogicalMeter(
    @Nullable Long id,
    String status,
    Location location,
    Date created,
    PropertyCollection propertyCollection
  ) {
    this(null, status, location, created, propertyCollection, Collections.emptyList(), null);
  }

  public LogicalMeter(
    @Nullable Long id,
    String status,
    Location location,
    Date created,
    PropertyCollection propertyCollection,
    List<PhysicalMeter> physicalMeters,
    @Nullable MeterDefinition meterDefinition
  ) {
    this.id = id;
    this.status = status;
    this.location = location;
    this.created = new Date(created.getTime());
    this.propertyCollection = propertyCollection;
    this.physicalMeters = Collections.unmodifiableList(physicalMeters);
    this.meterDefinition = meterDefinition;
  }

  public LogicalMeter createdAt(Date creationTime) {
    return new LogicalMeter(
      id,
      status,
      location,
      creationTime,
      propertyCollection,
      physicalMeters,
      meterDefinition
    );
  }

  public String getMedium() {
    return meterDefinition != null ? meterDefinition.medium : "Unknown medium";
  }

  public List<Quantity> getQuantities() {
    return meterDefinition != null ? meterDefinition.quantities : Collections.emptyList();
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
      status,
      location,
      created,
      propertyCollection,
      physicalMeters,
      meterDefinition
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
