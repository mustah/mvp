package com.elvaco.mvp.web.api;

import java.util.Map;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.MeterLocationUseCases;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestApi("/v1/api/summary")
public class SummaryController {

  private final MeterLocationUseCases meterLocationUseCases;

  @Autowired
  public SummaryController(MeterLocationUseCases meterLocationUseCases) {
    this.meterLocationUseCases = meterLocationUseCases;
  }

  @GetMapping("/meters")
  public MeterSummaryDto meterLocationSummary(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams).setAll(pathVars);
    MeterSummary summary = meterLocationUseCases.findAllForSummaryInfo(parameters);
    return new MeterSummaryDto(
      summary.numMeters(),
      summary.numCities(),
      summary.numAddresses()
    );
  }
}
