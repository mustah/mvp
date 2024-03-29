package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.PageableLimit;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import com.elvaco.mvp.web.exception.MeterNotFound;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;

@RequiredArgsConstructor
@RestApi("/api/v1/meters")
class LogicalMeterController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping
  public org.springframework.data.domain.Page<PagedLogicalMeterDto> logicalMeters(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    var page = logicalMeterUseCases.findAll(
      RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID),
      new PageableAdapter(pageable)
    );

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(LogicalMeterDtoMapper::toPagedDto);
  }

  @GetMapping("{id}")
  public LogicalMeterDto logicalMeterDetails(@PathVariable UUID id) {
    return logicalMeterUseCases.findById(id)
      .map(LogicalMeterDtoMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @DeleteMapping("{id}")
  public LogicalMeterDto deleteMeter(@PathVariable UUID id) {
    return logicalMeterUseCases.deleteById(id)
      .map(LogicalMeterDtoMapper::toDto)
      .orElseThrow(() -> new MeterNotFound(id));
  }

  @GetMapping("/legends")
  public List<LegendDto> legendItems(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);
    return logicalMeterUseCases.findAllLegendItemsBy(
      parameters,
      new PageableLimit(pageable, parameters.getLimit())
    );
  }
}
