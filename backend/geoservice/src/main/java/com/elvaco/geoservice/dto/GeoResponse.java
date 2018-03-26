package com.elvaco.geoservice.dto;

public class GeoResponse {
  private AddressDto address;
  private GeoDataDto geoData;

  public AddressDto getAddress() {
    return address;
  }

  public void setAddress(AddressDto address) {
    this.address = address;
  }

  public GeoDataDto getGeoData() {
    return geoData;
  }

  public void setGeoData(GeoDataDto geoData) {
    this.geoData = geoData;
  }

  @Override
  public String toString() {
    return address + "\n" + geoData;
  }
}
