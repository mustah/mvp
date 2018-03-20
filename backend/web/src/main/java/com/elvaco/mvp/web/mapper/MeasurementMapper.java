package com.elvaco.mvp.web.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import lombok.EqualsAndHashCode;

import static java.util.stream.Collectors.toList;

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

  public List<MeasurementSeriesDto> toSeries(List<Measurement> foundMeasurements) {
    Map<MeterQuantity, List<Measurement>> quantityMeasurements = new LinkedHashMap<>();
    for (Measurement measurement : foundMeasurements) {
      Quantity quantity = measurement.getQuantity();
      MeterQuantity key = new MeterQuantity(quantity, measurement.physicalMeter.id);
      if (!quantityMeasurements.containsKey(key)) {
        quantityMeasurements.put(key, new ArrayList<>());
      }
      quantityMeasurements.get(key).add(measurement);
    }

    List<MeasurementSeriesDto> series = new ArrayList<>();
    for (Map.Entry<MeterQuantity, List<Measurement>> entry : quantityMeasurements.entrySet()) {
      MeterQuantity key = entry.getKey();
      series.add(
        new MeasurementSeriesDto(
          key.quantity.name, key.quantity.unit, key.meterId,
          entry.getValue()
            .stream()
            .map(measurement -> new MeasurementValueDto(
              measurement.created.toInstant(),
              measurement.value
            ))
            .collect(toList())
        )
      );
    }
    return series;
  }

  @EqualsAndHashCode
  private static class MeterQuantity {

    Quantity quantity;
    UUID meterId;

    MeterQuantity(Quantity quantity, UUID meterId) {
      this.quantity = quantity;
      this.meterId = meterId;
    }
  }
}
