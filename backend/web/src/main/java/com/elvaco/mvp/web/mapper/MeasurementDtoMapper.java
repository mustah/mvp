package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class MeasurementDtoMapper {

  public static MeasurementDto toDto(Measurement measurement) {
    return new MeasurementDto(
      measurement.quantity,
      measurement.value,
      measurement.unit,
      measurement.created
    );
  }

  public static MeasurementSeriesDto toSeries(
    List<MeasurementValue> values,
    UUID logicalMeterId,
    String externalId,
    String city,
    String locationAddress,
    String mediumName,
    String physicalMeterAddress,
    QuantityParameter quantityParameter
  ) {
    return new MeasurementSeriesDto(
      logicalMeterId.toString(),
      quantityParameter.name,
      quantityParameter.unit,
      externalId + "-" + physicalMeterAddress,
      city,
      locationAddress,
      mediumName,
      values.stream()
        .map(measurement -> new MeasurementValueDto(measurement.when, measurement.value))
        .collect(toList())
    );
  }

  public static MeasurementSeriesDto toSeries(
    List<MeasurementValue> values,
    String label,
    String id,
    String city,
    QuantityParameter quantityParameter
  ) {
    return new MeasurementSeriesDto(
      id,
      quantityParameter.name,
      quantityParameter.unit,
      label,
      city,
      null,
      null,
      values.stream()
        .map(measurement -> new MeasurementValueDto(measurement.when, measurement.value))
        .collect(toList())
    );
  }
}
