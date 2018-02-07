package com.elvaco.mvp.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.dto.LogicalMeterDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.mapper.LogicalMeterMapper;
import com.elvaco.mvp.spring.PageableAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.util.ParametersHelper.combineParams;

@RestApi("/v1/api/meters")
public class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final LogicalMeterMapper logicalMeterMapper;

  @Autowired
  LogicalMeterController(
    LogicalMeterMapper logicalMeterMapper,
    LogicalMeterUseCases logicalMeterUseCases
  ) {
    this.logicalMeterMapper = logicalMeterMapper;
    this.logicalMeterUseCases = logicalMeterUseCases;
  }

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeter(@PathVariable Long id) {
    return logicalMeterMapper.toDto(logicalMeterUseCases.findById(id));
  }

  @GetMapping("/map-data")
  public List<MapMarkerDto> mapData() {
    return logicalMeterUseCases.findAll()
      .stream()
      .map(logicalMeterMapper::toMapMarkerDto)
      .collect(Collectors.toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<LogicalMeterDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterLogicalMeterDtos(combineParams(pathVars, requestParams), pageable);
  }

  private org.springframework.data.domain.Page<LogicalMeterDto> filterLogicalMeterDtos(
    Map<String, List<String>> filter,
    Pageable pageable
  ) {
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<LogicalMeter> page = logicalMeterUseCases.findAll(filter, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(logicalMeterMapper::toDto);
  }
}