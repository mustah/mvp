package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseDto;

import static java.util.Objects.nonNull;

public final class LocationMapper {

  static final IdNamedDto UNKNOWN_CITY = new IdNamedDto("Unknown city");
  static final IdNamedDto UNKNOWN_ADDRESS = new IdNamedDto("Unknown address");

  private LocationMapper() {}

  public static LocationWithId toLocationWithId(GeoResponseDto geoResponse, UUID logicalMeterId) {
    AddressDto address = geoResponse.address;
    GeoPositionDto geoData = geoResponse.geoData;
    GeoCoordinate coordinate = new GeoCoordinate(
      geoData.latitude,
      geoData.longitude,
      geoData.confidence
    );
    return new LocationWithId(
      logicalMeterId,
      coordinate,
      toLowerCaseOrNull(address.country),
      toLowerCaseOrNull(address.city),
      toLowerCaseOrNull(address.street)
    );
  }

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

  @Nullable
  private static String toLowerCaseOrNull(String str) {
    return str != null ? trimToNull(str) : null;
  }

  @Nullable
  private static String trimToNull(String str) {
    String trimmed = str.trim();
    return trimmed.isEmpty() ? null : trimmed.toLowerCase();
  }

  private static GeoPositionDto toGeoPositionDto(Location location) {
    return toGeoPosition(location).orElseGet(GeoPositionDto::new);
  }
}
