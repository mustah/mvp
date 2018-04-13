package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.GatewayMandatoryDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationMapper.UNKNOWN_LOCATION;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GatewayMapper {

  public GatewayDto toDto(Gateway gateway) {
    Optional<LogicalMeter> logicalMeter = gateway.meters.stream().findFirst();

    StatusLogEntry<UUID> gatewayStatusLog = gateway.currentStatus();

    return new GatewayDto(
      gateway.id,
      gateway.serial,
      gateway.productModel,
      gatewayStatusLog.status.name,
      formatUtc(gatewayStatusLog.start),
      new LocationDto(toCity(logicalMeter), toAddress(logicalMeter), toGeoPosition(logicalMeter)),
      connectedMeterIds(gateway)
    );
  }

  public GatewayMandatoryDto toGatewayMandatory(Gateway gateway) {
    StatusLogEntry<UUID> gatewayStatusLog = gateway.currentStatus();
    return new GatewayMandatoryDto(
      gateway.id,
      gateway.productModel,
      gateway.serial,
      gatewayStatusLog.status.name,
      formatUtc(gatewayStatusLog.start)
    );
  }

  public Gateway toDomainModel(GatewayDto gatewayDto, UUID organisationId) {
    return new Gateway(
      gatewayDto.id != null ? gatewayDto.id : randomUUID(),
      organisationId,
      gatewayDto.serial,
      gatewayDto.productModel
    );
  }

  public MapMarkerDto toMapMarkerDto(Gateway gateway) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = gateway.id;
    mapMarkerDto.status = gateway.currentStatus().status.name;
    gateway.meters
      .stream()
      .findFirst()
      .filter(lg -> lg.location.hasCoordinates())
      .map(lg -> lg.location.getCoordinate())
      .map(coordinate -> {
        mapMarkerDto.confidence = coordinate.getConfidence();
        mapMarkerDto.latitude = coordinate.getLatitude();
        mapMarkerDto.longitude = coordinate.getLongitude();
        return coordinate;
      });
    return mapMarkerDto;
  }

  private List<UUID> connectedMeterIds(Gateway gateway) {
    return gateway.meters
      .stream()
      .map(meter -> meter.id)
      .collect(toList());
  }

  private IdNamedDto toCity(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationMapper::toCity)
      .orElse(UNKNOWN_LOCATION);
  }

  private IdNamedDto toAddress(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationMapper::toAddress)
      .orElse(UNKNOWN_LOCATION);
  }

  private GeoPositionDto toGeoPosition(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(LocationMapper::toGeoPosition)
      .orElseGet(GeoPositionDto::new);
  }
}
