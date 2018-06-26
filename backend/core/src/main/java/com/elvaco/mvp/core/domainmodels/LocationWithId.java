package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LocationWithId extends Location implements Identifiable<UUID> {

  public final boolean shouldForceUpdate;

  private final UUID logicalMeterId;

  protected LocationWithId(
    UUID logicalMeterId,
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address,
    boolean shouldForceUpdate
  ) {
    super(coordinate, country, city, address);
    this.logicalMeterId = logicalMeterId;
    this.shouldForceUpdate = shouldForceUpdate;
  }

  @Override
  public UUID getId() {
    return logicalMeterId;
  }
}
