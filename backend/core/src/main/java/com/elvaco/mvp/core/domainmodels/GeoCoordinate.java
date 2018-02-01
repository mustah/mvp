package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GeoCoordinate {

  private final Double longitude;
  private final Double latitude;
  private final Double confidence;

  GeoCoordinate(Double latitude, Double longitude) {
    this(latitude, longitude, 1.0);
  }

  public GeoCoordinate(Double latitude, Double longitude, Double confidence) {
    this.latitude = latitude;
    this.longitude = longitude;
    if (confidence > 1.0 || confidence < 0.0) {
      throw new IllegalArgumentException("Confidence should be between 0.0 and 1.0 (inclusive)");
    }
    this.confidence = confidence;
  }

  public Double getConfidence() {
    return confidence;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }
}
