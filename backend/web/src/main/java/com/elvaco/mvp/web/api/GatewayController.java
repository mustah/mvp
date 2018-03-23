package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.exception.GatewayNotFound;
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

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
import static java.util.stream.Collectors.toList;

@RestApi("/api/v1/gateways")
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
      .map(gateway ->  gatewayMapper.toDto(gateway, TimeZone.getTimeZone("UTC")))
      .collect(toList());
  }

  @GetMapping("{id}")
  public GatewayDto gateway(TimeZone timeZone, @PathVariable String id) {
    return gatewayUseCases.findById(uuidOf(id))
      .map(gateway -> gatewayMapper.toDto(gateway, timeZone))
      .orElseThrow(() -> new GatewayNotFound(id));
  }

  @GetMapping("/map-markers")
  public List<MapMarkerDto> mapMarkers(@RequestParam MultiValueMap<String, String> requestParams) {
    return gatewayUseCases.findAll(requestParametersOf(requestParams))
      .stream()
      .map(gatewayMapper::toMapMarkerDto)
      .collect(toList());
  }

  @PostMapping
  public GatewayDto createGateway(TimeZone timeZone, @RequestBody GatewayDto gateway) {
    Gateway requestModel = gatewayMapper.toDomainModel(gateway, currentUser.getOrganisationId());
    return gatewayMapper.toDto(gatewayUseCases.save(requestModel), timeZone);
  }

  @GetMapping
  public org.springframework.data.domain.Page<GatewayDto> gateways(
    TimeZone timeZone,
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<Gateway> page = gatewayUseCases.findAll(parameters, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(gateway -> gatewayMapper.toDto(gateway, timeZone));
  }
}
