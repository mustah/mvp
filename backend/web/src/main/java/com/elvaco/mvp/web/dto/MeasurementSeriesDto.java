package com.elvaco.mvp.web.dto;

import java.util.List;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementSeriesDto {

  public String id;
  public String quantity;
  public String unit;
  public String label;
  @Nullable
  public String name;
  @Nullable
  public String medium;
  @Nullable
  public String meterId;
  public List<MeasurementValueDto> values;
}
