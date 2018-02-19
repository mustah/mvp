package com.elvaco.mvp.web.mapper;

import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.AddressDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.util.Dates;

import static java.util.stream.Collectors.toList;

public class LogicalMeterMapper {

  private final MeterStatusLogMapper meterStatusLogMapper;

  public LogicalMeterMapper(MeterStatusLogMapper meterStatusLogMapper) {
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  public MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = logicalMeter.id;
    mapMarkerDto.mapMarkerType = MapMarkerType.Meter;
    //TODO how to handle statuses?
    mapMarkerDto.status = new IdNamedDto("Ok");
    mapMarkerDto.status.name = "Ok";
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
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = Dates.formatTime(logicalMeter.created, timeZone);
    meterDto.id = logicalMeter.id;
    meterDto.address = new AddressDto();
    meterDto.address.name = logicalMeter.location.getStreetAddress().orElse("Unknown address");
    meterDto.city = new IdNamedDto();
    meterDto.city.name = logicalMeter.location.getCity().orElse("Unknown city");
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.position = new GeoPositionDto();
    if (logicalMeter.location.hasCoordinates()) {
      GeoCoordinate coordinate = logicalMeter.location.getCoordinate();
      meterDto.position.confidence = coordinate.getConfidence();
      meterDto.position.latitude = coordinate.getLatitude();
      meterDto.position.longitude = coordinate.getLongitude();
    }

    meterDto.statusChangelog = logicalMeter.meterStatusLogs
      .stream().map(
        (meterStatusLog) -> meterStatusLogMapper.toDto(meterStatusLog, timeZone)
      ).collect(toList());
    return meterDto;
  }
}
