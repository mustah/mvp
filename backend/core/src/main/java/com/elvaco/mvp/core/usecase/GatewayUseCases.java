package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Gateways;

public class GatewayUseCases {

  private final Gateways gateways;
  private final AuthenticatedUser currentUser;

  public GatewayUseCases(Gateways gateways, AuthenticatedUser currentUser) {
    this.gateways = gateways;
    this.currentUser = currentUser;
  }

  public List<Gateway> findAll() {
    if (currentUser.isSuperAdmin()) {
      return gateways.findAll();
    } else {
      return gateways.findAllByOrganisationId(currentUser.getOrganisationId());
    }
  }

  public Gateway save(Gateway gateway) {
    return gateways.save(gateway);
  }
}
