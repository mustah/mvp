package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

public class LocationBuilder {

  private UUID id;
  private String country;
  private String city;
  private String address;
  private GeoCoordinate coordinate;
  private Double latitude;
  private Double longitude;
  private Double confidence;
  private boolean shouldForceUpdate;

  public static LocationBuilder from(Location location) {
    return new LocationBuilder()
      .coordinate(location.getCoordinate())
      .country(location.getCountry())
      .city(location.getCity())
      .address(location.getAddress());
  }

  public LocationBuilder country(String country) {
    this.country = toLowerCaseOrNull(country);
    return this;
  }

  public LocationBuilder city(String city) {
    this.city = toLowerCaseOrNull(city);
    return this;
  }

  public LocationBuilder address(String address) {
    this.address = toLowerCaseOrNull(address);
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

  public LocationBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public LocationBuilder forceUpdate() {
    return shouldForceUpdate(true);
  }

  public LocationBuilder shouldForceUpdate(boolean shouldForceUpdate) {
    this.shouldForceUpdate = shouldForceUpdate;
    return this;
  }

  public Location build() {
    buildCoordinates();
    return new Location(coordinate, country, city, address);
  }

  public LocationWithId buildLocationWithId() {
    buildCoordinates();
    return new LocationWithId(id, coordinate, country, city, address, shouldForceUpdate);
  }

  private void buildCoordinates() {
    if (coordinate == null && hasLatLng()) {
      if (confidence == null) {
        coordinate = new GeoCoordinate(latitude, longitude);
      } else {
        coordinate = new GeoCoordinate(latitude, longitude, confidence);
      }
    }
  }

  private boolean hasLatLng() {
    return latitude != null && longitude != null;
  }

  @Nullable
  private static String toLowerCaseOrNull(String str) {
    return str != null ? trimToNull(str) : null;
  }

  @Nullable
  private static String trimToNull(String str) {
    String trimmed = str.trim();
    return trimmed.isEmpty() ? null : trimmed.toLowerCase();
  }
}
