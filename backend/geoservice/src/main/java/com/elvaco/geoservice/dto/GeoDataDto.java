package com.elvaco.geoservice.dto;

public class GeoDataDto {
  private String longitude;
  private String latitude;
  private double confidence;

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  @Override
  public String toString() {

    return latitude + ", " + longitude + " confidence: " + confidence;
  }
}
