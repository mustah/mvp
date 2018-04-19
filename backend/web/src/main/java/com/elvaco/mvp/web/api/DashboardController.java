package com.elvaco.mvp.web.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.DashboardUseCases;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
@RestApi("/api/v1/dashboards")
public class DashboardController {

  private final DashboardUseCases dashboardUseCases;

  @GetMapping("current")
  public DashboardDto getAllDashboards(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.requestParametersOf(requestParams)
      .setAll(pathVars);

    List<WidgetDto> widgets = getCollectionWidget(parameters)
      .map(Collections::singletonList)
      .orElse(emptyList());

    return new DashboardDto(randomUUID(), widgets);
  }

  private Optional<WidgetDto> getCollectionWidget(RequestParameters parameters) {
    return dashboardUseCases.getMeasurementsStatistics(parameters)
      .map(collectionStats -> new WidgetDto(
          WidgetType.COLLECTION.name,
          collectionStats.expected,
          OK.name,
          collectionStats.expected - collectionStats.actual
        )
      );
  }
}
