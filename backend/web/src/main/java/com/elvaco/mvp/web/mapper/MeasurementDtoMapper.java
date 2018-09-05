package com.elvaco.mvp.web.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
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

  public static List<MeasurementSeriesDto> toSeries(
    List<LabeledMeasurementValue> foundMeasurements
  ) {
    Map<LabeledQuantity, List<LabeledMeasurementValue>> quantityMeasurements =
      new LinkedHashMap<>();
    for (LabeledMeasurementValue measurement : foundMeasurements) {
      Quantity quantity = measurement.quantity;
      LabeledQuantity key = new LabeledQuantity(
        measurement.id,
        quantity,
        measurement.label,
        measurement.address,
        measurement.city
      );
      if (!quantityMeasurements.containsKey(key)) {
        quantityMeasurements.put(key, new ArrayList<>());
      }
      quantityMeasurements.get(key).add(measurement);
    }

    List<MeasurementSeriesDto> series = new ArrayList<>();
    for (Map.Entry<LabeledQuantity, List<LabeledMeasurementValue>> entry : quantityMeasurements
      .entrySet()) {
      LabeledQuantity key = entry.getKey();
      series.add(
        new MeasurementSeriesDto(
          key.id,
          key.quantity.name,
          key.quantity.presentationUnit(),
          key.label,
          key.city,
          key.address,
          entry.getValue()
            .stream()
            .map(measurement -> new MeasurementValueDto(
              measurement.when,
              measurement.value
            ))
            .collect(toList())
        )
      );
    }
    return series;
  }

  @EqualsAndHashCode
  private static class LabeledQuantity {
    String id;
    Quantity quantity;
    String label;
    String city;
    String address;

    private LabeledQuantity(
      String id,
      Quantity quantity,
      String label,
      String address,
      String city
    ) {
      this.id = id;
      this.quantity = quantity;
      this.label = label;
      this.city = city;
      this.address = address;
    }
  }
}
