package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.PageableLimit;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.CollectionStatsUseCases;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;

@RequiredArgsConstructor
@RestApi("/api/v1/meters/collection-stats")
class CollectionStatsController {

  private final CollectionStatsUseCases collectionStatsUseCases;

  @GetMapping
  public List<CollectionStatsDto> collectionStats(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID);
    return collectionStatsUseCases.findAll(
      parameters,
      new PageableLimit(pageable, parameters.getLimit())
    );
  }

  @GetMapping("/facility")
  public org.springframework.data.domain.Page<CollectionStatsDto> pagedCollectionStats(
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    var page = collectionStatsUseCases.findAllPaged(
      RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID),
      new PageableAdapter(pageable)
    );
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }

  @GetMapping("/date")
  public List<CollectionStatsPerDateDto> collectionStatsPerDate(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    return collectionStatsUseCases.findAllPerDate(
      RequestParametersAdapter.of(requestParams, LOGICAL_METER_ID)
    );
  }
}
