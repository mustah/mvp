package com.elvaco.geoservice.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

  public Integer errorCode;
  public String message;
  public AddressDto address;
}
