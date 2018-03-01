package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GatewayMapper {

  public GatewayDto toDto(Gateway gateway) {
    Optional<LogicalMeter> logicalMeter = gateway.meters.stream().findFirst();
    return new GatewayDto(
      gateway.id,
      gateway.serial,
      gateway.productModel,
      IdNamedDto.OK,
      toCity(logicalMeter),
      toAddress(logicalMeter),
      toGeoPosition(logicalMeter),
      emptyList(),
      logicalMeter.map(meter -> meter.id).orElse(null),
      null,
      logicalMeter.map(LogicalMeter::getManufacturer).orElse(null),
      toMeterStatus(logicalMeter),
      gateway.meters.stream().map(meter -> meter.id).collect(toList())
    );
  }

  public Gateway toDomainModel(GatewayDto gatewayDto, UUID organisationId) {
    return new Gateway(
      gatewayDto.id,
      organisationId,
      gatewayDto.serial,
      gatewayDto.productModel
    );
  }

  private IdNamedDto toCity(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(Location::getCity)
      .map(IdNamedDto::new)
      .orElse(new IdNamedDto("Unknown city"));
  }

  private IdNamedDto toAddress(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(Location::getStreetAddress)
      .map(IdNamedDto::new)
      .orElse(new IdNamedDto("Unknown address"));
  }

  private GeoPositionDto toGeoPosition(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .filter(Location::hasCoordinates)
      .map(Location::getCoordinate)
      .map(coordinate -> new GeoPositionDto(
        coordinate.getLatitude(),
        coordinate.getLongitude(),
        coordinate.getConfidence()
      ))
      .orElse(new GeoPositionDto());
  }

  private IdNamedDto toMeterStatus(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.flatMap(lg -> lg.meterStatusLogs.stream().findAny())
      .map(status -> new IdNamedDto(status.name))
      .orElse(IdNamedDto.UNKNOWN);
  }
}
