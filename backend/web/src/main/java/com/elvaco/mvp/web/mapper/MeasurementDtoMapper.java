package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
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
    Quantity quantity
  ) {
    return new MeasurementSeriesDto(
      logicalMeter.id.toString(),
      quantity.name,
      quantity.presentationUnit(),
      logicalMeter.externalId,
      logicalMeter.location.getCity(),
      logicalMeter.location.getAddress(),
      logicalMeter.meterDefinition.medium,
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
    Quantity quantity
  ) {
    return new MeasurementSeriesDto(
      id,
      quantity.name,
      quantity.presentationUnit(),
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
