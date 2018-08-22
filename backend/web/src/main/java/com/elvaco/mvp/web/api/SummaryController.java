package com.elvaco.mvp.web.api;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import com.elvaco.mvp.web.mapper.MeterSummaryDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RequiredArgsConstructor
@RestApi("/api/v1/summary")
public class SummaryController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping("/meters")
  public MeterSummaryDto meterLocationSummary(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = requestParametersOf(requestParams);
    return MeterSummaryDtoMapper.toDto(logicalMeterUseCases.summary(parameters));
  }
}
