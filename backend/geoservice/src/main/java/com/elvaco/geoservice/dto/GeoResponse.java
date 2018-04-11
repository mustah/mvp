package com.elvaco.geoservice.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GeoResponse {

  public AddressDto address;
  public GeoDataDto geoData;
}
