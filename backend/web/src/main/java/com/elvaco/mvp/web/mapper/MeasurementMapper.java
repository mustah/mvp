package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.web.dto.MeasurementDto;

public class MeasurementMapper {

  public MeasurementDto toDto(Measurement measurement) {
    return new MeasurementDto(
      measurement.id,
      measurement.quantity,
      measurement.value,
      measurement.unit,
      measurement.created
    );
  }
}
