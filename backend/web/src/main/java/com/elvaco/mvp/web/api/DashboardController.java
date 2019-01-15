package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
@RestApi("/api/v1/dashboards")
public class DashboardController {

  private final DashboardUseCases dashboardUseCases;

  @GetMapping("current")
  public DashboardDto getAllDashboards(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);

    return new DashboardDto(randomUUID(), findCollectionWidget(parameters));
  }

  private List<WidgetDto> findCollectionWidget(RequestParameters parameters) {
    return singletonList(
      dashboardUseCases.findCollectionStats(parameters)
        .map(collectionStats -> new WidgetDto(
          WidgetType.COLLECTION.name,
          collectionStats.collectionPercentage
        ))
        .orElse(new WidgetDto(WidgetType.COLLECTION.name, Double.NaN))
    );
  }
}

