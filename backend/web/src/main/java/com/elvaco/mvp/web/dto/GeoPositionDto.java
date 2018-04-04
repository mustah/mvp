package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class GeoPositionDto {

  public Double latitude;
  public Double longitude;
  public Double confidence;

  public GeoPositionDto(Double latitude, Double longitude, Double confidence) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }
}
