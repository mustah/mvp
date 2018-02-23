package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LocationDto {
  public IdNamedDto address;
  public IdNamedDto city;
  public GeoPositionDto position;
}
