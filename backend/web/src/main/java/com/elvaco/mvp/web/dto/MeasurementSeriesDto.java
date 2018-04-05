package com.elvaco.mvp.web.dto;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementSeriesDto {

  public String quantity;
  public String unit;
  public String label;
  public List<MeasurementValueDto> values;

  public MeasurementSeriesDto(
    String quantity,
    String unit,
    String label,
    List<MeasurementValueDto> values
  ) {
    Collections.sort(values);
    this.quantity = quantity;
    this.unit = unit;
    this.label = label;
    this.values = values;
  }
}
