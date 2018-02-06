package com.elvaco.mvp.repository.mappers;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.entity.meter.LocationEntity;

public class LocationMapper implements DomainEntityMapper<Location, LocationEntity> {
  @Override
  public Location toDomainModel(LocationEntity entity) {
    GeoCoordinate coordinate = null;

    if (entity.hasCoordinates()) {
      coordinate = new GeoCoordinate(entity.latitude, entity.longitude, entity.confidence);
    }

    return new LocationBuilder()
      .country(entity.country)
      .city(entity.city)
      .streetAddress(entity.streetAddress)
      .coordinate(coordinate)
      .build();
  }

  @Override
  public LocationEntity toEntity(Location domainModel) {
    LocationEntity entity = new LocationEntity();
    entity.country = domainModel.getCountry().orElse(null);
    entity.city = domainModel.getCity().orElse(null);
    entity.streetAddress = domainModel.getStreetAddress().orElse(null);
    if (domainModel.hasCoordinates()) {
      GeoCoordinate coordinate = domainModel.getCoordinate();
      entity.latitude = coordinate.getLatitude();
      entity.longitude = coordinate.getLongitude();
      entity.confidence = coordinate.getConfidence();
    }
    return entity;
  }
}
