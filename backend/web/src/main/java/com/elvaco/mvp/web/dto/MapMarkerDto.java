package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
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
}
