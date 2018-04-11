package com.elvaco.geoservice.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeoDataDto {

  public Double longitude;
  public Double latitude;
  public double confidence;
}
