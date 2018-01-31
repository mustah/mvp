package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Location {
  @Nullable
  private final Double latitude;
  @Nullable
  private final Double longitude;
  private final Double confidence;

  public Optional<Double> getLatitude() {
    return Optional.ofNullable(latitude);
  }

  @Nullable
  public Optional<Double> getLongitude() {
    return Optional.ofNullable(longitude);
  }

  public Double getConfidence() {
    if (longitude == null || latitude == null) {
      return 0.0;
    }

    return confidence;
  }

  public Location(Double latitude, Double longitude, Double confidence) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }

  public Location() {
    this(null, null, 0.0);
  }
}
