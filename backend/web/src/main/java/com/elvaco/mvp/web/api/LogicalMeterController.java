package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.mapper.LogicalMeterMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.web.util.ParametersHelper.combineParams;

@RestApi("/v1/api/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final LogicalMeterMapper logicalMeterMapper;
  private final ModelMapper modelMapper;

  @Autowired
  LogicalMeterController(
    LogicalMeterMapper logicalMeterMapper,
    LogicalMeterUseCases logicalMeterUseCases,
    ModelMapper modelMapper
  ) {
    this.logicalMeterMapper = logicalMeterMapper;
    this.logicalMeterUseCases = logicalMeterUseCases;
    this.modelMapper = modelMapper;
  }

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeter(TimeZone timeZone, @PathVariable Long id) {
    return logicalMeterMapper.toDto(logicalMeterUseCases.findById(id), timeZone);
  }

  @GetMapping("/map-data")
  public List<MapMarkerDto> mapData() {
    return logicalMeterUseCases.findAll()
      .stream()
      .map(logicalMeterMapper::toMapMarkerDto)
      .collect(Collectors.toList());
  }

  @GetMapping("{id}/measurements")
  public List<MeasurementDto> measurements(@PathVariable Long id) {
    LogicalMeter logicalMeter = logicalMeterUseCases.findById(id);
    return logicalMeterUseCases.measurements(logicalMeter)
      .stream()
      .map(m -> modelMapper.map(m, MeasurementDto.class))
      .collect(Collectors.toList());
  }

  @GetMapping("/all")
  public List<LogicalMeterDto> logicalMetersAll(
    TimeZone timeZone,
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return logicalMeterUseCases.findAll(combineParams(pathVars, requestParams))
      .stream()
      .map((logicalMeter) -> logicalMeterMapper.toDto(logicalMeter, timeZone))
      .collect(Collectors.toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<LogicalMeterDto> logicalMeters(
    TimeZone timeZone,
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterLogicalMeterDtos(combineParams(pathVars, requestParams), pageable, timeZone);
  }

  private org.springframework.data.domain.Page<LogicalMeterDto> filterLogicalMeterDtos(
    Map<String, List<String>> filter,
    Pageable pageable,
    TimeZone timeZone
  ) {
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAll(filter, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map((logicalMeter) -> logicalMeterMapper.toDto(logicalMeter, timeZone));
  }
}
