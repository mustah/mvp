package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
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
    mapMarkerDto.latitude = meteringPoint.location.getLatitude().orElse(null);
    mapMarkerDto.longitude = meteringPoint.location.getLongitude().orElse(null);
    mapMarkerDto.confidence = meteringPoint.location.getConfidence();

    return mapMarkerDto;
  }
}
