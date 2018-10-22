package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GeoCoordinate {

  public static final double HIGH_CONFIDENCE = 0.75;

  private final Double longitude;
  private final Double latitude;
  private final Double confidence;

  public GeoCoordinate(@Nullable Double latitude, @Nullable Double longitude) {
    this(latitude, longitude, 1.0);
  }

  public GeoCoordinate(
    @Nullable Double latitude,
    @Nullable Double longitude,
    @Nullable Double confidence
  ) {
    if (confidence == null || confidence > 1.0 || confidence < 0.0) {
      throw new IllegalArgumentException("Confidence should be between 0.0 and 1.0 (inclusive)");
    }
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }

  @Nullable
  public Double getLongitude() {
    return longitude;
  }

  @Nullable
  public Double getLatitude() {
    return latitude;
  }

  @Nullable
  public Double getConfidence() {
    return confidence;
  }

  public boolean isHighConfidence() {
    return confidence != null && confidence >= HIGH_CONFIDENCE;
  }
}
