package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

  public IdNamedDto city;
  public IdNamedDto address;
  public GeoPositionDto position;
}
