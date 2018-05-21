package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.database.entity.meter.LocationEntity;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;

@UtilityClass
public class LocationEntityMapper {

  public static Location toDomainModel(LocationEntity entity) {
    return entity != null
      ? new LocationBuilder()
      .country(entity.country)
      .city(entity.city)
      .address(entity.streetAddress)
      .coordinate(toGeoCoordinate(entity))
      .build()
      : UNKNOWN_LOCATION;
  }

  public static LocationWithId toLocationWithId(LocationEntity entity) {
    return new LocationBuilder()
      .id(entity.logicalMeterId)
      .country(entity.country)
      .city(entity.city)
      .address(entity.streetAddress)
      .coordinate(toGeoCoordinate(entity))
      .buildLocationWithId();
  }

  @Nullable
  public static LocationEntity toEntity(UUID logicalMeterId, @Nullable Location location) {
    if (location != null) {
      LocationEntity entity = new LocationEntity(
        logicalMeterId,
        location.getCountry(),
        location.getCity(),
        location.getAddress()
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

  public static LocationEntity toEntity(LocationWithId location) {
    return toEntity(location.getId(), location);
  }

  @Nullable
  private static GeoCoordinate toGeoCoordinate(LocationEntity entity) {
    return entity.hasCoordinates()
      ? new GeoCoordinate(entity.latitude, entity.longitude, entity.confidence)
      : null;
  }
}
