package com.elvaco.mvp.database.repository.mappers;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.database.entity.meter.LocationEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;

public class LocationMapper implements DomainEntityMapper<Location, LocationEntity> {

  @Override
  public Location toDomainModel(LocationEntity entity) {
    return entity != null
      ? new LocationBuilder()
      .country(entity.country)
      .city(entity.city)
      .streetAddress(entity.streetAddress)
      .coordinate(toGeoCoordinate(entity))
      .build()
      : UNKNOWN_LOCATION;
  }

  @Override
  public LocationEntity toEntity(Location location) {
    LocationEntity entity = new LocationEntity();
    entity.country = location.getCountry().orElse(null);
    entity.city = location.getCity().orElse(null);
    entity.streetAddress = location.getStreetAddress().orElse(null);
    if (location.hasCoordinates()) {
      GeoCoordinate coordinate = location.getCoordinate();
      entity.latitude = coordinate.getLatitude();
      entity.longitude = coordinate.getLongitude();
      entity.confidence = coordinate.getConfidence();
    }
    return entity;
  }

  @Nullable
  private GeoCoordinate toGeoCoordinate(LocationEntity entity) {
    return entity.hasCoordinates()
      ? new GeoCoordinate(entity.latitude, entity.longitude, entity.confidence)
      : null;
  }
}
