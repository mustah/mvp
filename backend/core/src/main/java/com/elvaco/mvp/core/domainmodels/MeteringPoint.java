package com.elvaco.mvp.core.domainmodels;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MeteringPoint {
  public final Long id;
  public final String status;
  public final Location location;
  public final Date created;
  public final PropertyCollection propertyCollection;

  public MeteringPoint(
    Long id,
    String status,
    Location location,
    Date created,
    PropertyCollection propertyCollection) {
    this.id = id;
    this.status = status;
    this.location = location;
    this.created = created == null ? null : (Date) created.clone();
    this.propertyCollection = propertyCollection;
  }
}
