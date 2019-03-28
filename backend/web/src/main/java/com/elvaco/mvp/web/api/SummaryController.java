package com.elvaco.mvp.web.api;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MeterSummaryDto;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.web.mapper.MeterSummaryDtoMapper.toDto;

@RequiredArgsConstructor
@RestApi("/api/v1/summary")
public class SummaryController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping
  public MeterSummaryDto meterLocationSummary(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return toDto(logicalMeterUseCases.summary(RequestParametersAdapter.of(requestParams)));
  }

  @GetMapping("/meters")
  public Long meterCount(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return logicalMeterUseCases.meterCount(RequestParametersAdapter.of(requestParams));
  }
}
