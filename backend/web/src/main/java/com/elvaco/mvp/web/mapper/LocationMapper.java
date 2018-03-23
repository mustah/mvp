package com.elvaco.mvp.web.mapper;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;

import static java.util.Objects.nonNull;

public final class LocationMapper {

  static final IdNamedDto UNKNOWN_CITY = new IdNamedDto("Unknown city");
  static final IdNamedDto UNKNOWN_ADDRESS = new IdNamedDto("Unknown address");

  private LocationMapper() {}

  static Optional<IdNamedDto> toCity(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getCity()))
      .map(l -> new IdNamedDto(l.getCity()));
  }

  static Optional<IdNamedDto> toAddress(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getAddress()))
      .map(l -> new IdNamedDto(l.getAddress()));
  }

  static Optional<GeoPositionDto> toGeoPosition(Location location) {
    return Optional.ofNullable(location.getCoordinate())
      .map(coordinate -> new GeoPositionDto(
        coordinate.getLatitude(),
        coordinate.getLongitude(),
        coordinate.getConfidence()
      ));
  }

  static LocationDto toLocationDto(Location location) {
    IdNamedDto address = toAddress(location).orElse(UNKNOWN_ADDRESS);
    IdNamedDto city = toCity(location).orElse(UNKNOWN_CITY);
    GeoPositionDto geoPosition = toGeoPositionDto(location);

    return new LocationDto(city, address, geoPosition);
  }

  private static GeoPositionDto toGeoPositionDto(Location location) {
    return toGeoPosition(location).orElseGet(GeoPositionDto::new);
  }
}
