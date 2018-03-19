package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.mapper.GatewayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/gateways")
public class GatewayController {

  private final GatewayUseCases gatewayUseCases;
  private final GatewayMapper gatewayMapper;
  private final AuthenticatedUser currentUser;

  @Autowired
  GatewayController(
    GatewayUseCases gatewayUseCases,
    GatewayMapper gatewayMapper,
    AuthenticatedUser currentUser
  ) {
    this.gatewayUseCases = gatewayUseCases;
    this.gatewayMapper = gatewayMapper;
    this.currentUser = currentUser;
  }

  @GetMapping("/all")
  public List<GatewayDto> findAllGateways() {
    return gatewayUseCases.findAll()
      .stream()
      .map(gatewayMapper::toDto)
      .collect(toList());
  }

  @GetMapping("/map-data")
  public List<MapMarkerDto> mapData() {
    return gatewayUseCases.findAll()
      .stream()
      .map(gatewayMapper::toMapMarkerDto)
      .collect(toList());
  }

  @PostMapping
  public GatewayDto createGateway(@RequestBody GatewayDto gateway) {
    Gateway requestModel = gatewayMapper.toDomainModel(gateway, currentUser.getOrganisationId());
    return gatewayMapper.toDto(gatewayUseCases.save(requestModel));
  }

  @GetMapping
  public org.springframework.data.domain.Page<GatewayDto> logicalMeters(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams).setAll(pathVars);
    return filterGatewayDtos(parameters, pageable);
  }

  private org.springframework.data.domain.Page<GatewayDto> filterGatewayDtos(
    RequestParameters parameters,
    Pageable pageable
  ) {
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<Gateway> page = gatewayUseCases.findAll(parameters, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(gatewayMapper::toDto);
  }
}
