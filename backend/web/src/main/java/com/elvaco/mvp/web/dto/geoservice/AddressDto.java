package com.elvaco.mvp.web.dto.geoservice;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

  public String country;
  public String city;
  public String street;
}
