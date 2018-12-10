package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MapMarkerDto {

  public UUID id;
  public double latitude;
  public double longitude;
  @Nullable
  public Integer alarm;
  @JsonIgnore
  public StatusType status;

  public MapMarkerDto(UUID id, double latitude, double longitude) {
    this(id, latitude, longitude, null);
  }

  public MapMarkerDto(UUID id, double latitude, double longitude, @Nullable Integer alarm) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.alarm = alarm;
  }
}
