package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeteringPoint {
  public final Long id;
  public final String status;
  public final Double latitude;
  public final Double longitude;
  public final Double confidence;

  public MeteringPoint(Long id, String status) {
    this(id, status, null, null, null);
  }

  public MeteringPoint(
      Long id,
      String status,
      Double latitude,
      Double longitude,
      Double confidence) {
    this.id = id;
    this.status = status;
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }
}
