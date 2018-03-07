package com.elvaco.mvp.web.mapper;

import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.util.Dates;

import static com.elvaco.mvp.web.dto.IdNamedDto.OK;
import static com.elvaco.mvp.web.dto.IdNamedDto.UNKNOWN;
import static com.elvaco.mvp.web.mapper.LocationMapper.toLocationDto;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LogicalMeterMapper {

  private final MeterStatusLogMapper meterStatusLogMapper;

  public LogicalMeterMapper(MeterStatusLogMapper meterStatusLogMapper) {
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  public MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = logicalMeter.id.toString();
    mapMarkerDto.mapMarkerType = MapMarkerType.Meter;
    //TODO how to handle statuses?
    mapMarkerDto.status = logicalMeter.meterStatusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> new IdNamedDto(meterStatusLog.name))
      .orElse(UNKNOWN);
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
    meterDto.id = logicalMeter.id.toString();
    meterDto.status = logicalMeter.meterStatusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> new IdNamedDto(meterStatusLog.name))
      .orElse(UNKNOWN);
    meterDto.flags = emptyList();
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.statusChanged = logicalMeter.meterStatusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> meterStatusLog.start)
      .map(date -> Dates.formatTime(date, timeZone))
      .orElse(created);
    meterDto.facility = logicalMeter.externalId;

    logicalMeter.gateways
      .stream()
      .findFirst()
      .map(gateway -> {
        meterDto.gatewayId = gateway.id.toString();
        meterDto.gatewaySerial = gateway.serial;
        meterDto.gatewayProductModel = gateway.productModel;
        meterDto.gatewayStatus = OK;
        return gateway;
      });

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.statusChangelog = logicalMeter.meterStatusLogs
      .stream()
      .map((meterStatusLog) -> meterStatusLogMapper.toDto(meterStatusLog, timeZone))
      .collect(toList());
    return meterDto;
  }
}
