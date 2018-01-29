package com.elvaco.mvp.dto;

import com.elvaco.mvp.core.dto.MapMarkerType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MapMarkerDto {
  public Long id;
  public MapMarkerType mapMarkerType;
  public IdNamedDto status;
  public double latitude;
  public double longitude;
  public double confidence;
}
