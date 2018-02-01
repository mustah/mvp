package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Location {

  @Nullable
  private final String country;
  @Nullable
  private final String city;
  @Nullable
  private final String streetAddress;
  @Nullable
  private final GeoCoordinate coordinate;

  public Location(
    GeoCoordinate coordinate,
    String country,
    String city,
    String streetAddress
  ) {
    this.coordinate = coordinate;
    this.country = country;
    this.city = city;
    this.streetAddress = streetAddress;
  }

  public Optional<String> getCountry() {
    return Optional.ofNullable(country);
  }

  public Optional<String> getCity() {
    return Optional.ofNullable(city);
  }

  public GeoCoordinate getCoordinate() {
    return coordinate;
  }

  public boolean hasCoordinates() {
    return coordinate != null;
  }

  public Optional<String> getStreetAddress() {
    return Optional.ofNullable(streetAddress);
  }
}
