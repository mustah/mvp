package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseDto;
import lombok.experimental.UtilityClass;

import static java.util.Objects.nonNull;

@UtilityClass
public class LocationDtoMapper {

  static final IdNamedDto UNKNOWN_LOCATION = new IdNamedDto("unknown");

  public static LocationWithId toLocationWithId(GeoResponseDto geoResponse, UUID logicalMeterId) {
    AddressDto address = geoResponse.address;
    GeoPositionDto geoData = geoResponse.geoData;
    return new LocationBuilder()
      .id(logicalMeterId)
      .country(address.country)
      .city(address.city)
      .address(address.street)
      .coordinate(new GeoCoordinate(
        geoData.latitude,
        geoData.longitude,
        geoData.confidence
      ))
      .buildLocationWithId();
  }

  public static LocationDto toLocationDto(Location location) {
    return new LocationDto(toCity(location), toAddress(location), toGeoPositionDto(location));
  }

  static IdNamedDto toCity(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getCity()))
      .map(l -> new IdNamedDto(l.getCity()))
      .orElse(UNKNOWN_LOCATION);
  }

  static IdNamedDto toAddress(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getAddress()))
      .map(l -> new IdNamedDto(l.getAddress()))
      .orElse(UNKNOWN_LOCATION);
  }

  static Optional<GeoPositionDto> toGeoPosition(Location location) {
    return Optional.ofNullable(location.getCoordinate())
      .map(coordinate -> new GeoPositionDto(
        coordinate.getLatitude(),
        coordinate.getLongitude(),
        coordinate.getConfidence()
      ));
  }

  private static GeoPositionDto toGeoPositionDto(Location location) {
    return toGeoPosition(location).orElseGet(GeoPositionDto::new);
  }
}
