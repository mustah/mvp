package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Location {

  public static final Location UNKNOWN_LOCATION = new Location(null, null, null, null, null);

  public static final String UNKNOWN = "unknown";

  private final String address;
  private final String city;
  private final String country;
  private final String zip;
  private final GeoCoordinate coordinate;

  protected Location(
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address,
    @Nullable String zip
  ) {
    this.coordinate = coordinate;
    this.country = country;
    this.city = city;
    this.address = address;
    this.zip = zip;
  }

  public Location(
    @Nullable Double latitude,
    @Nullable Double longitude,
    @Nullable Double confidence,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address,
    @Nullable String zip
  ) {
    this(
      GeoCoordinate.newOrNull(latitude, longitude, confidence),
      country,
      city,
      address,
      zip
    );
  }

  @Nullable
  public String getAddress() {
    return address;
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
  public String getZip() {
    return zip;
  }

  public String getAddressOrUnknown() {
    return address != null ? address : UNKNOWN;
  }

  public String getCityOrUnknown() {
    return city != null ? city : UNKNOWN;
  }

  public String getCountryOrUnknown() {
    return country != null ? country : UNKNOWN;
  }

  @Nullable
  public GeoCoordinate getCoordinate() {
    return coordinate;
  }

  public boolean hasCoordinates() {
    return coordinate != null;
  }

  public boolean hasNoCoordinates() {
    return coordinate == null;
  }

  public boolean isUnknown() {
    return country == null && city == null && address == null;
  }

  public boolean isKnown() {
    return country != null && city != null && address != null;
  }

  public boolean hasHighConfidence() {
    return getCoordinate() != null && getCoordinate().isHighConfidence();
  }
}
