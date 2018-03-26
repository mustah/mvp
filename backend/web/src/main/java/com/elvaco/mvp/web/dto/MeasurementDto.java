package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class MeasurementDto {

  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public ZonedDateTime created;

  public MeasurementDto(
    Long id,
    String quantity,
    double value,
    String unit,
    ZonedDateTime created
  ) {
    this.id = id;
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.created = created;
  }
}
