package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;

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

  public List<Gateway> findAll(RequestParameters parameters) {
    return gateways.findAll(setCurrentUsersOrganisationId(currentUser, parameters));
  }

  public Page<Gateway> findAll(RequestParameters parameters, Pageable pageable) {
    return gateways.findAll(setCurrentUsersOrganisationId(currentUser, parameters), pageable);
  }

  public Gateway save(Gateway gateway) {
    if (currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(gateway.organisationId)) {
      return gateways.save(gateway);
    }
    throw new Unauthorized("User is not authorized to save this entity");
  }

  public Optional<Gateway> findBy(UUID organisationId, String productModel, String serial) {
    return gateways.findBy(organisationId, productModel, serial);
  }

  public Optional<Gateway> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return gateways.findById(id);
    } else {
      return gateways.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }
}
