package com.elvaco.mvp.web.mapper;

import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.util.Dates;

import static java.util.stream.Collectors.toList;

public class LogicalMeterMapper {

  private static final IdNamedDto OK = new IdNamedDto("Ok");

  private final MeterStatusLogMapper meterStatusLogMapper;

  public LogicalMeterMapper(MeterStatusLogMapper meterStatusLogMapper) {
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  public MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = logicalMeter.id;
    mapMarkerDto.mapMarkerType = MapMarkerType.Meter;
    //TODO how to handle statuses?
    mapMarkerDto.status = OK;
    if (logicalMeter.location.hasCoordinates()) {
      GeoCoordinate coord = logicalMeter.location.getCoordinate();
      if (coord != null) {
        mapMarkerDto.confidence = coord.getConfidence();
        mapMarkerDto.latitude = coord.getLatitude();
        mapMarkerDto.longitude = coord.getLongitude();
      }
    }
    return mapMarkerDto;
  }

  public LogicalMeterDto toDto(LogicalMeter logicalMeter, TimeZone timeZone) {
    String created = Dates.formatTime(logicalMeter.created, timeZone);
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = created;
    meterDto.id = logicalMeter.id;
    String address = logicalMeter.location.getStreetAddress().orElse("Unknown address");
    String city = logicalMeter.location.getCity().orElse("Unknown city");
    meterDto.address = new IdNamedDto(address);
    meterDto.city = new IdNamedDto(city);
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.statusChanged = logicalMeter.meterStatusLogs.stream()
      .findAny()
      .map(meterStatusLog -> meterStatusLog.start)
      .map(date -> Dates.formatTime(date, timeZone))
      .orElse(created);
    meterDto.position = new GeoPositionDto();
    meterDto.facility = logicalMeter.externalId;
    logicalMeter.gateways
      .stream()
      .findFirst()
      .map(gateway -> {
        meterDto.gatewayId = gateway.id;
        meterDto.gatewaySerial = gateway.serial;
        meterDto.gatewayProductModel = gateway.productModel;
        meterDto.gatewayStatus = OK;
        return gateway;
      });

    if (logicalMeter.location.hasCoordinates()) {
      GeoCoordinate coordinate = logicalMeter.location.getCoordinate();
      meterDto.position.confidence = coordinate.getConfidence();
      meterDto.position.latitude = coordinate.getLatitude();
      meterDto.position.longitude = coordinate.getLongitude();
    }

    meterDto.statusChangelog = logicalMeter.meterStatusLogs
      .stream()
      .map((meterStatusLog) -> meterStatusLogMapper.toDto(meterStatusLog, timeZone))
      .collect(toList());
    return meterDto;
  }
}
