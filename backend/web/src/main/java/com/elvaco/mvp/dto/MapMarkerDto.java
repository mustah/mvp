package com.elvaco.mvp.dto;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.dto.MapMarkerType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MapMarkerDto {
  public Long id;
  public MapMarkerType mapMarkerType;
  public IdNamedDto status;
  @Nullable
  public Double latitude;
  @Nullable
  public Double longitude;
  @Nullable
  public Double confidence;
}
