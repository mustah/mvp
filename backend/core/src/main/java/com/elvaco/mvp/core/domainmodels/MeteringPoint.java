package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MeteringPoint {
  public final Long id;
  public final String status;
  public final Location location;
  public final PropertyCollection propertyCollection;

  public MeteringPoint(
    Long id,
    String status,
    Location location,
    PropertyCollection propertyCollection) {
    this.id = id;
    this.status = status;
    this.location = location;
    this.propertyCollection = propertyCollection;
  }
}
