package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GeoPositionDto {
  public Double latitude;
  public Double longitude;
  public double confidence;
}
