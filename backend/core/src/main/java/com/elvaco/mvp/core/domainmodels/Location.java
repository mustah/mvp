package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Location {

  public static final Location UNKNOWN_LOCATION = new Location(null, null, null, null);

  private final String streetAddress;
  private final String city;
  private final String country;
  private final GeoCoordinate coordinate;

  Location(
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String streetAddress
  ) {
    this.coordinate = coordinate;
    this.country = country;
    this.city = city;
    this.streetAddress = streetAddress;
  }

  @Nullable
  public String getStreetAddress() {
    return streetAddress;
  }

  @Nullable
  public String getCity() {
    return city;
  }

  @Nullable
  public String getCountry() {
    return country;
  }

  @Nullable
  public GeoCoordinate getCoordinate() {
    return coordinate;
  }

  public boolean hasCoordinates() {
    return coordinate != null;
  }
}
