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
    String facilityId,
    String mediumName,
    String physicalMeterAddress,
    QuantityParameter quantityParameter
  ) {
    return MeasurementSeriesDto.builder()
      .id(logicalMeterId.toString())
      .quantity(quantityParameter.name)
      .unit(quantityParameter.unit)
      .label(facilityId + "-" + physicalMeterAddress)
      .name(facilityId)
      .meterId(physicalMeterAddress)
      .medium(mediumName)
      .values(toSortedMeasurements(values))
      .build();
  }

  public static MeasurementSeriesDto toSeries(
    List<MeasurementValue> values,
    String label,
    String id,
    QuantityParameter quantityParameter
  ) {
    return MeasurementSeriesDto.builder()
      .id(id)
      .quantity(quantityParameter.name)
      .unit(quantityParameter.unit)
      .label(label)
      .values(toSortedMeasurements(values))
      .build();
  }

  public static List<MeasurementValueDto> toSortedMeasurements(List<MeasurementValue> values) {
    return values.stream()
      .map(measurement -> new MeasurementValueDto(measurement.when, measurement.value))
      .sorted()
      .collect(toList());
  }
}
