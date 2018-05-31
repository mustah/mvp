package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.StatusType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MapMarkersDto {

  public Map<StatusType, List<MapMarkerDto>> markers;
}
