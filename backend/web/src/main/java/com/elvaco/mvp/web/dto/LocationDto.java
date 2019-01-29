package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

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
  @Nullable
  public GeoPositionDto position;
}
