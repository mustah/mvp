package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class MeterSummaryDto {

  public int numMeters;
  public int numCities;
  public int numAddresses;

  public MeterSummaryDto(int numMeters, int numCities, int numAddresses) {
    this.numMeters = numMeters;
    this.numCities = numCities;
    this.numAddresses = numAddresses;
  }
}
