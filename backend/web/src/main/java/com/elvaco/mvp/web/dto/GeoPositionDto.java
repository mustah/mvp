package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GeoPositionDto {

  public Double latitude;
  public Double longitude;
  public Double confidence;

  public GeoPositionDto() {}

  public GeoPositionDto(Double latitude, Double longitude, Double confidence) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }
}
