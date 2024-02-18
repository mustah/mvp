package com.elvaco.mvp.core.domainmodels;

import java.io.Serial;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Address extends IdentifiableType<String> {

  @Serial private static final long serialVersionUID = 3678911504223141123L;
  
  public final String street;
  public final String zip;
  public final String city;
  public final String country;

  @Override
  public String getId() {
    return country + "," + city + "," + street;
  }
}
