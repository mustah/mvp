package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/gateways")
public class GatewayController {

  private final GatewayUseCases gatewayUseCases;

  @Autowired
  GatewayController(GatewayUseCases gatewayUseCases) {
    this.gatewayUseCases = gatewayUseCases;
  }

  @GetMapping
  public List<GatewayDto> findAllGateways() {
    return gatewayUseCases.findAll()
      .stream()
      .map(gateway -> new GatewayDto(gateway.id, gateway.serial, gateway.productModel))
      .collect(toList());
  }
}
