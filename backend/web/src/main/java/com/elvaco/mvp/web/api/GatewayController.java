package com.elvaco.mvp.web.api;

import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.exception.GatewayNotFound;
import com.elvaco.mvp.web.mapper.GatewayDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RequiredArgsConstructor
@RestApi("/api/v1/gateways")
public class GatewayController {

  private final GatewayUseCases gatewayUseCases;

  @GetMapping("{id}")
  public GatewayDto gateway(@PathVariable UUID id) {
    return gatewayUseCases.findById(id)
      .map(GatewayDtoMapper::toDto)
      .orElseThrow(() -> new GatewayNotFound(id));
  }

  @GetMapping
  public org.springframework.data.domain.Page<GatewayDto> gateways(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<Gateway> page = gatewayUseCases.findAll(parameters, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(GatewayDtoMapper::toDto);
  }
}
