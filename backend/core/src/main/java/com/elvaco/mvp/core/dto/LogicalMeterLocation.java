package com.elvaco.mvp.core.dto;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LogicalMeterLocation {
  public final UUID id;
  public final Location location;

  @SuppressWarnings("unused")
  public LogicalMeterLocation(
    UUID id,
    Double latitude,
    Double longitude,
    Double confidence,
    String country,
    String city,
    String streetAddress
  ) {
    this.id = id;
    this.location = new LocationBuilder()
      .latitude(latitude)
      .longitude(longitude)
      .confidence(confidence)
      .country(country)
      .city(city)
      .address(streetAddress).build();
  }
}
