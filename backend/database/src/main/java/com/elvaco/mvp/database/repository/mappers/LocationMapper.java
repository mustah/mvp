package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.database.entity.meter.LocationEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;

public class LocationMapper {

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

  @Nullable
  public LocationEntity toEntity(Location location, UUID logicalMeterId) {
    if (location != null) {
      LocationEntity entity = new LocationEntity(
        logicalMeterId,
        location.getCountry(),
        location.getCity(),
        location.getStreetAddress()
      );
      if (location.hasCoordinates()) {
        GeoCoordinate coordinate = location.getCoordinate();
        entity.latitude = coordinate.getLatitude();
        entity.longitude = coordinate.getLongitude();
        entity.confidence = coordinate.getConfidence();
      }
      return entity;
    } else {
      return null;
    }
  }

  @Nullable
  private GeoCoordinate toGeoCoordinate(LocationEntity entity) {
    return entity.hasCoordinates()
      ? new GeoCoordinate(entity.latitude, entity.longitude, entity.confidence)
      : null;
  }
}
