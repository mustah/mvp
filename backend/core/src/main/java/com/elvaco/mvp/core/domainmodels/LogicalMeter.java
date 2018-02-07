package com.elvaco.mvp.core.domainmodels;

import java.util.Date;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class LogicalMeter {

  @Nullable
  public final Long id;
  public final String status;
  public final Location location;
  public final Date created;
  public final PropertyCollection propertyCollection;

  public LogicalMeter(
    String status,
    Location location,
    Date created,
    PropertyCollection propertyCollection
  ) {
    this(null, status, location, created, propertyCollection);
  }

  public LogicalMeter(
    @Nullable
    Long id,
    String status,
    Location location,
    Date created,
    PropertyCollection propertyCollection
  ) {
    this.id = id;
    this.status = status;
    this.location = location;
    this.created = (Date) created.clone();
    this.propertyCollection = propertyCollection;
  }
}