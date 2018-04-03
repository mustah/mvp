package com.elvaco.geoservice.dto;

public class GeoDataDto {

  public Double longitude;
  public Double latitude;
  public double confidence;

  public GeoDataDto() {}

  public GeoDataDto(Double longitude, Double latitude, double confidence) {
    this.longitude = longitude;
    this.latitude = latitude;
    this.confidence = confidence;
  }

  @Override
  public String toString() {
    return latitude + ", " + longitude + " confidence: " + confidence;
  }
}
