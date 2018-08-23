package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MeasurementDto {

  public String quantity;
  public double value;
  public String unit;
  public ZonedDateTime created;
}
