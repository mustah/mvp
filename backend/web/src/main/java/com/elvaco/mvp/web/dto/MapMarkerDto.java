package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class MapMarkerDto {

  public UUID id;
  public String status;
  @Nullable
  public Double latitude;
  @Nullable
  public Double longitude;
  @Nullable
  public Double confidence;

  public MapMarkerDto(
    UUID id,
    String status,
    @Nullable Double latitude,
    @Nullable Double longitude,
    @Nullable Double confidence
  ) {
    this.id = id;
    this.status = status;
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }
}
