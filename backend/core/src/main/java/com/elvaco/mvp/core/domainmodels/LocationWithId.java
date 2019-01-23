package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LocationWithId extends Location implements Identifiable<PrimaryKey> {

  public final boolean shouldForceUpdate;

  private final PrimaryKey pk;

  protected LocationWithId(
    PrimaryKey pk,
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address,
    @Nullable String zip,
    boolean shouldForceUpdate
  ) {
    super(coordinate, country, city, address, zip);
    this.pk = pk;
    this.shouldForceUpdate = shouldForceUpdate;
  }

  @Override
  public PrimaryKey getId() {
    return pk;
  }
}
