package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GatewayStatusLog;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.GatewayMandatoryDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.util.Dates;

import static com.elvaco.mvp.web.mapper.LocationMapper.UNKNOWN_ADDRESS;
import static com.elvaco.mvp.web.mapper.LocationMapper.UNKNOWN_CITY;
import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GatewayMapper {

  public GatewayDto toDto(Gateway gateway, TimeZone timeZone) {
    Optional<LogicalMeter> logicalMeter = gateway.meters.stream().findFirst();

    Optional<GatewayStatusLog> gatewayStatusLog = getCurrentStatus(gateway.statusLogs);

    return new GatewayDto(
      gateway.id.toString(),
      gateway.serial,
      gateway.productModel,
      getStatusName(gatewayStatusLog),
      getStatusChanged(gatewayStatusLog, timeZone),
      new LocationDto(toCity(logicalMeter), toAddress(logicalMeter), toGeoPosition(logicalMeter)),
      connectedMeterIds(gateway)
    );
  }

  public GatewayMandatoryDto toGatewayMandatory(Gateway gateway, TimeZone timeZone) {
    Optional<GatewayStatusLog> gatewayStatusLog = getCurrentStatus(gateway.statusLogs);
    return new GatewayMandatoryDto(
      gateway.id.toString(),
      gateway.productModel,
      gateway.serial,
      getStatusName(gatewayStatusLog),
      getStatusChanged(gatewayStatusLog, timeZone)
    );
  }

  public Gateway toDomainModel(GatewayDto gatewayDto, UUID organisationId) {
    return new Gateway(
      uuidOf(gatewayDto.id),
      organisationId,
      gatewayDto.serial,
      gatewayDto.productModel
    );
  }

  public MapMarkerDto toMapMarkerDto(Gateway gateway) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = gateway.id.toString();
    mapMarkerDto.mapMarkerType = MapMarkerType.Gateway;
    mapMarkerDto.status = getStatusName(getCurrentStatus(gateway.statusLogs));
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

  private String getStatusChanged(Optional<GatewayStatusLog> gatewayStatusLog, TimeZone timeZone) {
    return gatewayStatusLog.map(status -> Dates.formatTime(status.start, timeZone))
      .orElse("");
  }

  private String getStatusName(Optional<GatewayStatusLog> gatewayStatusLog) {
    return gatewayStatusLog.map(statusLog -> statusLog.status)
      .map(statusType -> statusType.name)
      .orElse(StatusType.UNKNOWN.name);
  }

  private Optional<GatewayStatusLog> getCurrentStatus(List<GatewayStatusLog> statusLogs) {
    return statusLogs.stream().findFirst();
  }

  private List<UUID> connectedMeterIds(Gateway gateway) {
    return gateway.meters
      .stream()
      .map(meter -> meter.id)
      .collect(toList());
  }

  private IdNamedDto toCity(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(LocationMapper::toCity)
      .orElse(UNKNOWN_CITY);
  }

  private IdNamedDto toAddress(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(LocationMapper::toAddress)
      .orElse(UNKNOWN_ADDRESS);
  }

  private GeoPositionDto toGeoPosition(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(LocationMapper::toGeoPosition)
      .orElseGet(GeoPositionDto::new);
  }
}
