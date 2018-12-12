package com.elvaco.mvp.database.entity.meter;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;

public class LogicalMeterWithLocation extends IdentifiableType<PrimaryKey> {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final LocationEntity location;
  public final String medium;
  public final String utcOffset;

  private final PrimaryKey primaryKey;

  public LogicalMeterWithLocation(
    UUID id,
    UUID organisationId,
    String externalId,
    String utcOffset,
    String country,
    String city,
    String address,
    String medium
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.utcOffset = utcOffset;
    this.location = LocationEntity.builder()
      .pk(new EntityPk(id, organisationId))
      .country(country)
      .city(city)
      .streetAddress(address)
      .build();
    this.medium = medium;
    this.primaryKey = new LogicalMeterPk(id, organisationId);
  }

  @Override
  public PrimaryKey getId() {
    return primaryKey;
  }
}
