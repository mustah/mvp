package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
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
    LogicalMeter logicalMeter,
    String physicalMeterAddress,
    QuantityParameter quantityParameter
  ) {
    return new MeasurementSeriesDto(
      logicalMeter.id.toString(),
      quantityParameter.name,
      quantityParameter.unit,
      logicalMeter.externalId + "-" + physicalMeterAddress,
      logicalMeter.location.getCity(),
      logicalMeter.location.getAddress(),
      logicalMeter.meterDefinition.medium.name.toString(),
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
