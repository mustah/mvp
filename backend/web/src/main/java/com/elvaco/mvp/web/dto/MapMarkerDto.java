package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.dto.MapMarkerType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MapMarkerDto {

  public String id;
  public MapMarkerType mapMarkerType;
  public String status;
  @Nullable
  public Double latitude;
  @Nullable
  public Double longitude;
  @Nullable
  public Double confidence;
}
