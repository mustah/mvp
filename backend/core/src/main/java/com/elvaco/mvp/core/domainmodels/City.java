package com.elvaco.mvp.core.domainmodels;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class City extends IdentifiableType<String> {

  public final String name;
  public final String country;

  @Override
  public String getId() {
    return country + "," + name;
  }
}
