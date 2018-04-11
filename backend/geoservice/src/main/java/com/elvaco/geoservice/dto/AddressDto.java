package com.elvaco.geoservice.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

  public String street;
  public String city;
  public String country;
}
