package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.GatewayMandatoryDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationDtoMapper.UNKNOWN_LOCATION;
import static java.util.stream.Collectors.toList;

@UtilityClass
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GatewayDtoMapper {

  public static GatewayDto toDto(GatewaySummaryDto gateway) {
    StatusLogEntry gatewayStatusLog = Optional.ofNullable(gateway.statusLog)
      .orElseGet(() -> StatusLogEntry.unknownFor(gateway));

    return new GatewayDto(
      gateway.id,
      gateway.serial,
      formatProductModel(gateway.productModel),
      gatewayStatusLog.status.name,
      formatUtc(gatewayStatusLog.start),
      LocationDtoMapper.toLocationDto(gateway.meterLocations.stream()
        .findFirst()
        .map(meter -> meter.location)
        .orElse(
          Location.UNKNOWN_LOCATION)),
      gateway.meterLocations.stream().map(meter -> meter.id).collect(toList()),
      gateway.organisationId,
      null
    );
  }

  public static GatewayDto toDto(Gateway gateway) {
    Optional<LogicalMeter> logicalMeter = gateway.meters.stream().findFirst();

    StatusLogEntry gatewayStatusLog = gateway.currentStatus();

    return new GatewayDto(
      gateway.id,
      gateway.serial,
      formatProductModel(gateway.productModel),
      gatewayStatusLog.status.name,
      formatUtc(gatewayStatusLog.start),
      new LocationDto(
        toCountry(logicalMeter),
        toCity(logicalMeter),
        toAddress(logicalMeter),
        toZip(logicalMeter),
        toGeoPosition(logicalMeter)
      ),
      connectedMeterIds(gateway),
      gateway.organisationId,
      gateway.extraInfo.deepCopy()
    );
  }

  static GatewayMandatoryDto toGatewayMandatory(Gateway gateway) {
    StatusLogEntry gatewayStatusLog = gateway.currentStatus();
    return GatewayMandatoryDto.builder()
      .id(gateway.id)
      .productModel(formatProductModel(gateway.productModel))
      .serial(gateway.serial)
      .status(new IdNamedDto(gatewayStatusLog.status.name))
      .statusChanged(formatUtc(gatewayStatusLog.start))
      .ip(gateway.ip)
      .phoneNumber(gateway.phoneNumber)
      .extraInfo(gateway.extraInfo)
      .build();
  }

  private static List<UUID> connectedMeterIds(Gateway gateway) {
    return gateway.meters.stream()
      .map(meter -> meter.id)
      .collect(toList());
  }

  private static String toCountry(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationDtoMapper::toCountry)
      .orElse(UNKNOWN_LOCATION)
      .name;
  }

  private static String toCity(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationDtoMapper::toCity)
      .orElse(UNKNOWN_LOCATION)
      .name;
  }

  private static String toAddress(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationDtoMapper::toAddress)
      .orElse(UNKNOWN_LOCATION)
      .name;
  }

  private static String toZip(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .map(LocationDtoMapper::toZip)
      .orElse(UNKNOWN_LOCATION)
      .name;
  }

  private static GeoPositionDto toGeoPosition(Optional<LogicalMeter> logicalMeter) {
    return logicalMeter.map(meter -> meter.location)
      .flatMap(LocationDtoMapper::toGeoPositionDto)
      .orElseGet(GeoPositionDto::new);
  }

  private static String formatProductModel(String productModel) {
    return productModel == null || productModel.trim().isEmpty() ? "Unknown" : productModel.trim();
  }
}
