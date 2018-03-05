package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

public class PhysicalMeterUseCases {

  private final AuthenticatedUser authenticatedUser;
  private final PhysicalMeters physicalMeters;

  public PhysicalMeterUseCases(AuthenticatedUser authenticatedUser, PhysicalMeters physicalMeters) {
    this.authenticatedUser = authenticatedUser;
    this.physicalMeters = physicalMeters;
  }

  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter)) {
      return physicalMeters.save(physicalMeter);
    }
    throw new Unauthorized("User '" + authenticatedUser.getUsername() + "' is not allowed to "
                             + "update this physical meter");
  }

  public Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  ) {
    if (authenticatedUser.isSuperAdmin()
      || authenticatedUser.isWithinOrganisation(organisationId)) {
      return physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
        organisationId,
        externalId,
        address
      );
    }
    return Optional.empty();
  }

  private boolean hasTenantAccess(PhysicalMeter physicalMeter) {
    return authenticatedUser.isSuperAdmin()
      || authenticatedUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
