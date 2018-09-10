package com.elvaco.mvp.web.dto;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementSeriesDto {

  public String id;
  public String quantity;
  public String unit;
  public String label;
  public String address;
  public String city;
  public String medium;
  public List<MeasurementValueDto> values;

  public MeasurementSeriesDto(
    String id,
    String quantity,
    String unit,
    String label,
    @Nullable String city,
    @Nullable String address,
    @Nullable String medium,
    List<MeasurementValueDto> values
  ) {
    Collections.sort(values);
    this.id = id;
    this.quantity = quantity;
    this.unit = unit;
    this.label = label;
    this.address = address;
    this.city = city;
    this.values = values;
    this.medium = medium;
  }

  public MeasurementSeriesDto(
    String id,
    String quantity,
    String unit,
    String label,
    String medium,
    List<MeasurementValueDto> values
  ) {
    this(id, quantity, unit, label, null, null, medium, values);
  }

  public MeasurementSeriesDto(
    String id,
    String quantity,
    String unit,
    String label,
    List<MeasurementValueDto> values
  ) {
    this(id, quantity, unit, label, null, null, null, values);
  }
}
