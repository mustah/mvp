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

  public String country;
  public String city;
  public String address;
  public String zip;
  public GeoPositionDto position;
}
