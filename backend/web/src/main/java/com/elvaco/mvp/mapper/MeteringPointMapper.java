package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.dto.MeteringPointDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MeteringPointMapper {

  private final ModelMapper modelMapper;

  @Autowired
  public MeteringPointMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public MapMarkerDto toMapMarkerDto(MeteringPoint meteringPoint) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = meteringPoint.id;
    mapMarkerDto.mapMarkerType = MapMarkerType.Meter;
    //TODO how to handle statuses?
    mapMarkerDto.status = new IdNamedDto(meteringPoint.status);
    mapMarkerDto.status.name = meteringPoint.status;
    if (meteringPoint.location.hasCoordinates()) {
      GeoCoordinate coord = meteringPoint.location.getCoordinate();
      mapMarkerDto.confidence = coord.getConfidence();
      mapMarkerDto.latitude = coord.getLatitude();
      mapMarkerDto.longitude = coord.getLongitude();
    }
    return mapMarkerDto;
  }

  public MeteringPointDto toDto(MeteringPoint meteringPoint) {
    return modelMapper.map(meteringPoint, MeteringPointDto.class);
  }
}
