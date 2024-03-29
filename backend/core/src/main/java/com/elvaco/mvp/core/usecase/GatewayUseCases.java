package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.GatewaysMeters;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GatewayUseCases {

  private final Gateways gateways;
  private final GatewaysMeters gatewaysMeters;
  private final AuthenticatedUser currentUser;

  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    return gateways.findSerials(parameters.ensureOrganisationFilters(currentUser), pageable);
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

  public Optional<Gateway> findBy(UUID organisationId, String serial) {
    return gateways.findBy(organisationId, serial);
  }

  public List<Gateway> findBy(String serial) {
    return gateways.findBy(serial);
  }

  public Optional<Gateway> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return gateways.findById(id);
    } else {
      return gateways.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  public void setLastSeenForMeter(
    Gateway gateway,
    LogicalMeter logicalMeter,
    ZonedDateTime lastSeen
  ) {
    gatewaysMeters.saveOrUpdate(gateway.id, logicalMeter.id, logicalMeter.organisationId, lastSeen);
  }
}
