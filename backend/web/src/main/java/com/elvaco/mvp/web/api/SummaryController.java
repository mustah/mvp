package com.elvaco.mvp.web.api;

import java.util.Map;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import com.elvaco.mvp.web.mapper.MeterSummaryDtoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RestApi("/api/v1/summary")
public class SummaryController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @Autowired
  public SummaryController(LogicalMeterUseCases logicalMeterUseCases) {
    this.logicalMeterUseCases = logicalMeterUseCases;
  }

  @GetMapping("/meters")
  public MeterSummaryDto meterLocationSummary(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    return MeterSummaryDtoMapper.toDto(logicalMeterUseCases.summary(parameters));
  }
}
