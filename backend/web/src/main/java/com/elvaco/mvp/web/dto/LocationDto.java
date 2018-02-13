package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LocationDto {
  public AddressDto address;
  public IdNamedDto city;
  public GeoPositionDto position;
}
