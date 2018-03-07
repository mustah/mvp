package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;
import java.util.Date;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MeasurementDto {

  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public ZonedDateTime created;

  public MeasurementDto(Long id, String quantity, double value, String unit, ZonedDateTime created) {
    this.id = id;
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.created = ZonedDateTime.from(created);
  }
}
