package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapMarkerDtoMapper {

  public static MapMarkerDto toDto(MapMarker marker) {
    return new MapMarkerDto(
      marker.id,
      marker.latitude,
      marker.longitude,
      marker.status != null ? marker.status : StatusType.UNKNOWN
    );
  }
}
