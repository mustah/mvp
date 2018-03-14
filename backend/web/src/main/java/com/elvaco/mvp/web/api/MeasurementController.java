package com.elvaco.mvp.web.api;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.web.dto.MeasurementAggregateDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import com.elvaco.mvp.web.exception.MeasurementNotFound;
import com.elvaco.mvp.web.exception.NoPhysicalMetersException;
import com.elvaco.mvp.web.exception.QuantityNotFound;
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
    @RequestParam TemporalResolution resolution
  ) {
    RequestParameters requestParams = new RequestParametersAdapter()
      .setAll("id", meters.stream()
        .map(UUID::toString)
        .collect(toList())
      );
    List<LogicalMeter> logicalMeters = logicalMeterUseCases.findAll(requestParams);

    Map.Entry<Quantity, List<UUID>> entry =
      LogicalMeterHelper.mapMeterQuantitiesToPhysicalMeterUuids(
        logicalMeters,
        Collections.singletonList(new Quantity(
          quantityName,
          unit
        ))
      ).entrySet()
        .stream()
        .findAny()
        .orElseThrow(() -> new QuantityNotFound(quantityName));

    if (entry.getValue().isEmpty()) {
      throw new NoPhysicalMetersException();
    }

    Quantity quantity = entry.getKey();
    List<MeasurementValueDto> measurementValueDtos = measurementUseCases.averageForPeriod(
      entry.getValue(),
      quantity.name,
      quantity.unit,
      from,
      to,
      resolution
    ).stream().map(
      (measurementValue) -> new MeasurementValueDto(measurementValue.when, measurementValue.value)
    ).collect(toList());

    return new MeasurementAggregateDto(quantity.name, quantity.unit, measurementValueDtos);
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
