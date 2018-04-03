package com.elvaco.mvp.web.dto.geoservice;

import com.elvaco.mvp.web.dto.GeoPositionDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeoResponseDto {

  public AddressDto address;
  public GeoPositionDto geoData;
}
