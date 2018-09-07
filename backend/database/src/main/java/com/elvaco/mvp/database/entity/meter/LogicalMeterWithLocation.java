package com.elvaco.mvp.database.entity.meter;

import java.util.UUID;

public class LogicalMeterWithLocation {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final LocationEntity location;
  public final String medium;

  public LogicalMeterWithLocation(
    UUID id,
    UUID organisationId,
    String externalId,
    String country,
    String city,
    String address,
    String medium
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.location = new LocationEntity(id, country, city, address);
    this.medium = medium;
  }
}
