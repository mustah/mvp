package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class MeterSummaryDto {

  public long numMeters;
  public long numCities;
  public long numAddresses;

  public MeterSummaryDto(long numMeters, long numCities, long numAddresses) {
    this.numMeters = numMeters;
    this.numCities = numCities;
    this.numAddresses = numAddresses;
  }
}
