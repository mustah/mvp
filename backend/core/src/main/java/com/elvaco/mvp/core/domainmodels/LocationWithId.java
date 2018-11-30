package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LocationWithId extends Location implements Identifiable<PrimaryKey> {

  public final boolean shouldForceUpdate;

  private final PrimaryKey pk;

  protected LocationWithId(
    UUID logicalMeterId,
    UUID organisationId,
    @Nullable GeoCoordinate coordinate,
    @Nullable String country,
    @Nullable String city,
    @Nullable String address,
    boolean shouldForceUpdate
  ) {
    super(coordinate, country, city, address);
    this.pk = new Pk(logicalMeterId, organisationId);
    this.shouldForceUpdate = shouldForceUpdate;
  }

  @Override
  public PrimaryKey getId() {
    return pk;
  }
}
