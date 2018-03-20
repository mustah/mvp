package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementSeriesDto {

  public String quantity;
  public String unit;
  public String meter;
  public List<MeasurementValueDto> values;

  public MeasurementSeriesDto(
    String quantity,
    String unit,
    UUID meter,
    List<MeasurementValueDto> values
  ) {
    this.quantity = quantity;
    this.unit = unit;
    this.meter = meter.toString();
    this.values = values;
  }
}
