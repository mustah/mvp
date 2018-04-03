package com.elvaco.mvp.web.dto.geoservice;

import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class GeoResponseErrorDto {

  public Integer errorCode;
  public String message;
  public AddressDto address;

  public GeoResponseErrorDto(Integer errorCode, String message, AddressDto address) {
    this.errorCode = errorCode;
    this.message = message;
    this.address = address;
  }
}
