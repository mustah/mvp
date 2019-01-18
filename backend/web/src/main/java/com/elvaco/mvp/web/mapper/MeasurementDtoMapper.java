package com.elvaco.mvp.web.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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

  public static List<MeasurementSeriesDto> toSeries(
    List<LabeledMeasurementValue> foundMeasurements
  ) {
    Map<LabeledQuantity, List<LabeledMeasurementValue>> quantityMeasurements =
      new LinkedHashMap<>();

    foundMeasurements.forEach(measurement -> {
      LabeledQuantity key = new LabeledQuantity(
        measurement.id,
        measurement.quantity,
        measurement.label,
        measurement.address,
        measurement.city,
        measurement.medium
      );
      quantityMeasurements.computeIfAbsent(key, (v) -> new ArrayList<>());
      quantityMeasurements.get(key).add(measurement);
    });

    return quantityMeasurements.entrySet().stream()
      .map(entry -> new MeasurementSeriesDto(
        entry.getKey().id,
        entry.getKey().quantity.name,
        entry.getKey().quantity.presentationUnit(),
        entry.getKey().label,
        entry.getKey().city,
        entry.getKey().address,
        entry.getKey().medium,
        entry.getValue().stream()
          .map(measurement -> new MeasurementValueDto(measurement.when, measurement.value))
          .collect(toList())
      ))
      .collect(toList());
  }

  @EqualsAndHashCode
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static class LabeledQuantity {
    String id;
    Quantity quantity;
    String label;
    String address;
    String city;
    String medium;
  }
}
