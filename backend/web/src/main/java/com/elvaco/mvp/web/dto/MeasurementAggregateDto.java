package com.elvaco.mvp.web.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementAggregateDto {

  public String quantity;
  public String unit;
  public List<MeasurementValueDto> measurementValues;

  public MeasurementAggregateDto(
    String quantity,
    String unit,
    List<MeasurementValueDto> measurementValues
  ) {

    this.quantity = quantity;
    this.unit = unit;
    this.measurementValues = measurementValues;
  }
}
