package com.elvaco.mvp.web.mapper;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
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

  public static LocationWithId toLocationWithId(GeoResponseDto geoResponse,
                                                LogicalMeter logicalMeter) {
    AddressDto address = geoResponse.address;
    GeoPositionDto geoData = geoResponse.geoData;
    return LocationBuilder.from(logicalMeter.location)
      .id(logicalMeter.id)
      .organisationId(logicalMeter.organisationId)
      .coordinate(new GeoCoordinate(
        geoData.latitude,
        geoData.longitude,
        geoData.confidence
      ))
      .buildLocationWithId();
  }

  public static LocationDto toLocationDto(Location location) {
    return new LocationDto(
      toCountry(location).name,
      toCity(location).name,
      toAddress(location).name,
      toZip(location).name,
      toGeoPositionDto(location).orElse(null)
    );
  }

  static IdNamedDto toCountry(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getCountry()))
      .map(l -> new IdNamedDto(l.getCountry()))
      .orElse(UNKNOWN_LOCATION);
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

  static IdNamedDto toZip(Location location) {
    return Optional.ofNullable(location)
      .filter(l -> nonNull(l.getZip()))
      .map(l -> new IdNamedDto(l.getZip()))
      .orElse(UNKNOWN_LOCATION);
  }

  static Optional<GeoPositionDto> toGeoPositionDto(Location location) {
    return Optional.ofNullable(location.getCoordinate())
      .map(coordinate -> new GeoPositionDto(
        coordinate.getLatitude(),
        coordinate.getLongitude(),
        coordinate.getConfidence()
      ));
  }
}
