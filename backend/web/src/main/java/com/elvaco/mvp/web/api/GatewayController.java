package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.mapper.GatewayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

  @GetMapping
  public List<GatewayDto> findAllGateways() {
    return gatewayUseCases.findAll()
      .stream()
      .map(gatewayMapper::toDto)
      .collect(toList());
  }

  @PostMapping
  public GatewayDto createGateway(@RequestBody GatewayDto gateway) {
    Gateway requestModel = gatewayMapper.toDomainModel(gateway, currentUser.getOrganisationId());
    return gatewayMapper.toDto(gatewayUseCases.save(requestModel));
  }
}
