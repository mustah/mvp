package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterMapper;
import com.elvaco.mvp.web.mapper.MeasurementMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static java.util.stream.Collectors.toList;

@RestApi("/api/v1/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final LogicalMeterMapper logicalMeterMapper;
  private final MeasurementMapper measurementMapper;

  @Autowired
  LogicalMeterController(
    LogicalMeterMapper logicalMeterMapper,
    LogicalMeterUseCases logicalMeterUseCases,
    MeasurementMapper measurementMapper
  ) {
    this.logicalMeterMapper = logicalMeterMapper;
    this.logicalMeterUseCases = logicalMeterUseCases;
    this.measurementMapper = measurementMapper;
  }

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeter(@PathVariable UUID id) {
    return logicalMeterUseCases.findByIdWithMeasurements(id)
      .map(logicalMeterMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @GetMapping("/map-markers")
  public List<MapMarkerDto> mapMarkers(@RequestParam MultiValueMap<String, String> requestParams) {
    return logicalMeterUseCases.findAll(requestParametersOf(requestParams))
      .stream()
      .map(logicalMeterMapper::toMapMarkerDto)
      .collect(toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<LogicalMeterDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAll(parameters, adapter);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(logicalMeterMapper::toDto);
  }

  private Supplier<RequestParameters> lazyRequestParameters(LogicalMeter logicalMeter) {
    return () -> {
      RequestParameters parameters = new RequestParametersAdapter();

      logicalMeter.physicalMeters
        .stream()
        .filter(m -> m.id != null)
        .forEach(m -> parameters.add("meterId", m.id.toString()));

      logicalMeter.getQuantities()
        .forEach(quantity -> parameters.add("quantity", quantity.name));

      return parameters;
    };
  }

}
