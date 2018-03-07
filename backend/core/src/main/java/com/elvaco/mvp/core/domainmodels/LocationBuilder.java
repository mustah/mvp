package com.elvaco.mvp.core.domainmodels;

public class LocationBuilder {

  private String country;
  private String city;
  private String streetAddress;
  private GeoCoordinate coordinate;
  private Double latitude;
  private Double longitude;
  private Double confidence;

  public LocationBuilder country(String country) {
    this.country = country;
    return this;
  }

  public LocationBuilder city(String city) {
    this.city = city;
    return this;
  }

  public LocationBuilder streetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
    return this;
  }

  public LocationBuilder latitude(Double latitude) {
    this.latitude = latitude;
    return this;
  }

  public LocationBuilder longitude(Double longitude) {
    this.longitude = longitude;
    return this;
  }

  public LocationBuilder confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  public LocationBuilder coordinate(GeoCoordinate coordinate) {
    this.coordinate = coordinate;
    return this;
  }

  public Location build() {
    if (coordinate == null && hasLatLng()) {
      if (confidence == null) {
        coordinate = new GeoCoordinate(latitude, longitude);
      } else {
        coordinate = new GeoCoordinate(latitude, longitude, confidence);
      }
    }
    return new Location(coordinate, country, city, streetAddress);
  }

  private boolean hasLatLng() {
    return latitude != null && longitude != null;
  }
}
