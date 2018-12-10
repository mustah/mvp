package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.util.Dates;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementDto {

  public String id;
  public String quantity;
  public double value;
  public String unit;
  public ZonedDateTime created;

  public MeasurementDto(
    String quantity,
    double value,
    String unit,
    ZonedDateTime created
  ) {
    this(quantity.concat("_").concat(Dates.formatUtc(created)), quantity, value, unit, created);
  }
}
