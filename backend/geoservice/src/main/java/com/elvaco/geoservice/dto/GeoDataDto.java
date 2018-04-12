package com.elvaco.geoservice.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeoDataDto {

  public Double longitude;
  public Double latitude;
  public double confidence;
}
