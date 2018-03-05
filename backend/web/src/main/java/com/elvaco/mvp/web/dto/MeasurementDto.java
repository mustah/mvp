package com.elvaco.mvp.web.dto;

import java.util.Date;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MeasurementDto {

  public Long id;
  public String quantity;
  public double value;
  public String unit;
  public Date created;

  public MeasurementDto(Long id, String quantity, double value, String unit, Date created) {
    this.id = id;
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.created = new Date(created.getTime());
  }
}
