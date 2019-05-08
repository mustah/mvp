package com.elvaco.mvp.web.api;

import java.util.UUID;

import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.exception.GatewayNotFound;
import com.elvaco.mvp.web.mapper.GatewayDtoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
