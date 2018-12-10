package com.elvaco.mvp.database.repository.mappers;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;

@UtilityClass
public class LocationEntityMapper {

  public static Location toDomainModel(LocationEntity location) {
    return location != null
      ? new LocationBuilder()
      .country(location.country)
      .city(location.city)
      .address(location.streetAddress)
      .coordinate(toGeoCoordinate(location))
      .build()
      : UNKNOWN_LOCATION;
  }

  public static LocationWithId toLocationWithId(LocationEntity entity) {
    return new LocationBuilder()
      .id(entity.pk.id)
      .organisationId(entity.pk.organisationId)
      .country(entity.country)
      .city(entity.city)
      .address(entity.streetAddress)
      .coordinate(toGeoCoordinate(entity))
      .buildLocationWithId();
  }

  @Nullable
  public static LocationEntity toEntity(PrimaryKey pk, @Nullable Location location) {
    if (location != null) {
      LocationEntity entity = LocationEntity.builder()
        .pk(new EntityPk(pk.getId(), pk.getOrganisationId()))
        .country(location.getCountry())
        .city(location.getCity())
        .streetAddress(location.getAddress())
        .build();
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
