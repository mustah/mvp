package com.elvaco.mvp.web.dto;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class MeasurementValueDto {

  public Instant when;
  public Double value;

  public MeasurementValueDto(Instant when, Double value) {
    this.when = when;
    this.value = value;
  }
}
