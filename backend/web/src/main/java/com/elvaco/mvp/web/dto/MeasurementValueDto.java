package com.elvaco.mvp.web.dto;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class MeasurementValueDto implements Comparable<MeasurementValueDto> {

  public Instant when;
  public Double value;

  public MeasurementValueDto(Instant when, Double value) {
    this.when = when;
    this.value = value;
  }

  @Override
  public int compareTo(MeasurementValueDto o) {
    if (o.when.isBefore(when)) {
      return 1;
    } else if (o.when.isAfter(when)) {
      return -1;
    }
    return 0;
  }
}
