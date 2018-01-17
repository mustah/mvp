package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.IdNamedDto;
import com.elvaco.mvp.core.dto.MapMarkerDto;
import com.elvaco.mvp.core.dto.MapMarkerType;
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
    mapMarkerDto.status = new IdNamedDto();
    mapMarkerDto.status.name = meteringPoint.status;
    mapMarkerDto.latitude = meteringPoint.latitude;
    mapMarkerDto.longitude = meteringPoint.longitude;
    mapMarkerDto.confidence = meteringPoint.confidence;

    return mapMarkerDto;
  }
}
