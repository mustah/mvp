package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;

import static com.elvaco.mvp.web.mapper.LocationMapper.UNKNOWN_ADDRESS;
import static com.elvaco.mvp.web.mapper.LocationMapper.UNKNOWN_CITY;
import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GatewayMapper {

  public GatewayDto toDto(Gateway gateway) {
    Optional<LogicalMeter> logicalMeter = gateway.meters.stream().findFirst();
    return new GatewayDto(
      gateway.id.toString(),
      gateway.serial,
      gateway.productModel,
      Status.OK,
      new LocationDto(toCity(logicalMeter), toAddress(logicalMeter), toGeoPosition(logicalMeter)),
      connectedMeterIds(gateway)
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

  private List<String> connectedMeterIds(Gateway gateway) {
    return gateway.meters
      .stream()
      .map(meter -> meter.id.toString())
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
