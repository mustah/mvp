package com.elvaco.mvp.web.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.web.dto.MeasurementAggregateDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import com.elvaco.mvp.web.exception.LogicalMeterMissingQuantityException;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import com.elvaco.mvp.web.exception.NoPhysicalMetersException;
import com.elvaco.mvp.web.mapper.MeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.toList;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestApi("/v1/api/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final MeasurementMapper measurementMapper;

  @Autowired
  MeasurementController(
    MeasurementUseCases measurementUseCases,
    LogicalMeterUseCases logicalMeterUseCases,
    MeasurementMapper measurementMapper
  ) {
    this.measurementUseCases = measurementUseCases;
    this.logicalMeterUseCases = logicalMeterUseCases;
    this.measurementMapper = measurementMapper;
  }

  @GetMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return measurementUseCases.findById(id)
      .map(measurementMapper::toDto)
      .orElseThrow(() -> new MeasurementNotFound(id));
  }

  @GetMapping("/average")
  public MeasurementAggregateDto average(
    @RequestParam List<UUID> meters,
    @RequestParam(name = "quantity") String quantityName,
    @RequestParam(required = false) String unit,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime from,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) ZonedDateTime to,
    @RequestParam String resolution
  ) {
    RequestParametersAdapter requestParams = new RequestParametersAdapter();
    meters.forEach(meterId -> requestParams.add("id", meterId.toString()));
    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAll(requestParams);
    List<UUID> physicalMeterUuids = new ArrayList<>();
    for (LogicalMeter logicalMeter : logicalMeters) {
      Quantity quantity = logicalMeter.getQuantity(quantityName)
        .orElseThrow(() -> new LogicalMeterMissingQuantityException(logicalMeter.id, quantityName));

      if (unit == null) {
        unit = quantity.unit;
      }
      physicalMeterUuids.addAll(logicalMeter.physicalMeters.stream()
                                  .map(physicalMeter -> physicalMeter.id)
                                  .collect(toList()));
    }
    if (physicalMeterUuids.isEmpty()) {
      throw new NoPhysicalMetersException();
    }
    List<MeasurementValueDto> measurementValueDtos = measurementUseCases.averageForPeriod(
      physicalMeterUuids,
      quantityName,
      unit,
      from,
      to,
      resolution
    ).stream().map(
      (measurementValue) -> new MeasurementValueDto(measurementValue.when, measurementValue.value)
    ).collect(toList());

    return new MeasurementAggregateDto(quantityName, unit, measurementValueDtos);
  }

  @GetMapping
  public List<MeasurementDto> measurements(
    @RequestParam(value = "scale", required = false) String scale,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return measurementUseCases.findAll(scale, RequestParametersAdapter.of(requestParams))
      .stream()
      .map(measurementMapper::toDto)
      .collect(toList());
  }
}
