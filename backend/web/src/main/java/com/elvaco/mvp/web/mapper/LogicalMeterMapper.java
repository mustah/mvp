package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogicalMeterMapper {

  private final ModelMapper modelMapper;

  @Autowired
  public LogicalMeterMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = logicalMeter.id;
    mapMarkerDto.mapMarkerType = MapMarkerType.Meter;
    //TODO how to handle statuses?
    mapMarkerDto.status = new IdNamedDto(logicalMeter.status);
    mapMarkerDto.status.name = logicalMeter.status;
    if (logicalMeter.location.hasCoordinates()) {
      GeoCoordinate coord = logicalMeter.location.getCoordinate();
      mapMarkerDto.confidence = coord.getConfidence();
      mapMarkerDto.latitude = coord.getLatitude();
      mapMarkerDto.longitude = coord.getLongitude();
    }
    return mapMarkerDto;
  }

  public LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    return modelMapper.map(logicalMeter, LogicalMeterDto.class);
  }
}
