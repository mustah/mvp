package com.elvaco.mvp.core.domainmodels;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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
  private Date created;
  @Nullable
  private MeterDefinition meterDefinition;

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
      Location.unknownLocation(),
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

  public Date getCreated() {
    return new Date(created.getTime());
  }

  public void setCreated(Date created) {
    this.created = new Date(created.getTime());
  }

  public String getMedium() {
    return meterDefinition != null ? meterDefinition.getMedium() : "Unknown medium";
  }

  public List<Quantity> getQuantities() {
    return meterDefinition != null ? meterDefinition.getQuantities() : Collections.emptyList();
  }

  @Nullable
  public MeterDefinition getMeterDefinition() {
    return meterDefinition;
  }

  public void setMeterDefinition(@Nullable MeterDefinition meterDefinition) {
    this.meterDefinition = meterDefinition;
  }

  public boolean hasMeterDefinition() {
    return meterDefinition != null;
  }
}
