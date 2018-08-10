package com.elvaco.mvp.core.domainmodels;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Address extends IdentifiableType<String> {

  public final String street;
  public final String city;
  public final String country;

  @Override
  public String getId() {
    return country + "," + city + "," + street;
  }
}
