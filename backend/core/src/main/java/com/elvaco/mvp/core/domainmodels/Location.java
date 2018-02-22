package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Location {

  public static final Location UNKNOWN_LOCATION = new Location(null, null, null, null);

  @Nullable
  private final String streetAddress;
  @Nullable
  private final String city;
  @Nullable
  private final String country;
  @Nullable
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

  public Optional<String> getStreetAddress() {
    return Optional.ofNullable(streetAddress);
  }

  public Optional<String> getCity() {
    return Optional.ofNullable(city);
  }

  public Optional<String> getCountry() {
    return Optional.ofNullable(country);
  }

  @Nullable
  public GeoCoordinate getCoordinate() {
    return coordinate;
  }

  public boolean hasCoordinates() {
    return coordinate != null;
  }
}
