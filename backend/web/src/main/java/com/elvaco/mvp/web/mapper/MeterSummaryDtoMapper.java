package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.web.dto.MeterSummaryDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MeterSummaryDtoMapper {

  public static MeterSummaryDto toDto(MeterSummary summary) {
    return new MeterSummaryDto(summary.meters, summary.cities, summary.addresses);
  }

  public static MeterSummary toDomainModel(MeterSummaryDto summary) {
    return new MeterSummary(summary.numMeters, summary.numCities, summary.numAddresses);
  }
}
