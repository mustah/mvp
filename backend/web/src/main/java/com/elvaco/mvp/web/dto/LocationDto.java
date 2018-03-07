package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class LocationDto {

  public IdNamedDto city;
  public IdNamedDto address;
  public GeoPositionDto position;

  public LocationDto(IdNamedDto city, IdNamedDto address, GeoPositionDto position) {
    this.city = city;
    this.address = address;
    this.position = position;
  }
}
