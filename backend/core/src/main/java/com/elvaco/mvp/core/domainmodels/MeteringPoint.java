package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MeteringPoint {
  public final Long id;
  public final String status;
  public final Double latitude;
  public final Double longitude;
  public final Double confidence;
  public final PropertyCollection propertyCollection;

  public MeteringPoint(Long id, String status) {
    this(id, status, null, null, null, null);
  }

  public MeteringPoint(
    Long id,
    String status,
    Double latitude,
    Double longitude,
    Double confidence,
    PropertyCollection propertyCollection) {
    this.id = id;
    this.status = status;
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
    this.propertyCollection = propertyCollection;
  }
}
