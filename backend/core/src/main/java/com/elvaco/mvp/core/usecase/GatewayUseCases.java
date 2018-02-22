package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;

public class GatewayUseCases {

  private final Gateways gateways;

  public GatewayUseCases(Gateways gateways) {
    this.gateways = gateways;
  }

  public List<Gateway> findAll() {
    return gateways.findAll();
  }

  public Gateway save(Gateway gateway) {
    return gateways.save(gateway);
  }
}
