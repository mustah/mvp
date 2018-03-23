package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
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
import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
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
  public LogicalMeterDto logicalMeter(@PathVariable String id) {
    return logicalMeterUseCases.findById(uuidOf(id))
      .map(logicalMeter -> logicalMeterMapper.toDto(logicalMeter, TimeZone.getTimeZone("UTC")))
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @GetMapping("/map-data")
  public List<MapMarkerDto> mapData(@RequestParam MultiValueMap<String, String> requestParams) {
    return logicalMeterUseCases.findAll(requestParametersOf(requestParams))
      .stream()
      .map(logicalMeterMapper::toMapMarkerDto)
      .collect(toList());
  }

  @GetMapping("{id}/measurements")
  public List<MeasurementDto> measurements(@PathVariable String id) {
    LogicalMeter logicalMeter = logicalMeterUseCases
      .findById(uuidOf(id))
      .orElseThrow(() -> new MeterNotFound(id));
    return logicalMeterUseCases.measurements(logicalMeter, lazyRequestParameters(logicalMeter))
      .stream()
      .map(measurementMapper::toDto)
      .collect(toList());
  }

  @GetMapping("/all")
  public List<LogicalMeterDto> logicalAllMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    return logicalMeterUseCases.findAll(parameters)
      .stream()
      .map((logicalMeter) -> logicalMeterMapper.toDto(logicalMeter, TimeZone.getTimeZone("UTC")))
      .collect(toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<LogicalMeterDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    return filterLogicalMeterDtos(parameters, pageable);
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

  private org.springframework.data.domain.Page<LogicalMeterDto> filterLogicalMeterDtos(
    RequestParameters parameters,
    Pageable pageable
  ) {
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAll(parameters, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map((logicalMeter) -> logicalMeterMapper.toDto(logicalMeter, TimeZone.getTimeZone("UTC")));
  }
}
