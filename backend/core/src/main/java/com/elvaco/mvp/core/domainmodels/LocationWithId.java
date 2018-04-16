package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LocationWithId extends Location implements Identifiable<UUID> {

  private final UUID logicalMeterId;

  protected LocationWithId(
    UUID logicalMeterId,
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address
  ) {
    super(coordinate, country, city, address);
    this.logicalMeterId = logicalMeterId;
  }

  public static LocationWithId of(Location location, UUID logicalMeterId) {
    return new LocationWithId(
      logicalMeterId,
      location.getCoordinate(),
      location.getCountry(),
      location.getCity(),
      location.getAddress()
    );
  }

  @Override
  public UUID getId() {
    return logicalMeterId;
  }
}
