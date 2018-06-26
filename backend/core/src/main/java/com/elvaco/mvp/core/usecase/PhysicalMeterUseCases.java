package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;

@RequiredArgsConstructor
public class PhysicalMeterUseCases {

  private final AuthenticatedUser authenticatedUser;
  private final PhysicalMeters physicalMeters;

  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    if (!hasTenantAccess(physicalMeter)) {
      throw new Unauthorized(String.format(
        "User '%s' is not allowed to "
        + "update physical meter with ID %s",
        authenticatedUser.getUsername(),
        physicalMeter.id
      ));
    }
    return physicalMeters.save(physicalMeter);
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

  public Page<PhysicalMeter> findAll(
    RequestParameters parameters,
    Pageable pageable
  ) {
    return physicalMeters.findAll(
      setCurrentUsersOrganisationId(authenticatedUser, parameters),
      pageable
    );
  }

  private boolean hasTenantAccess(PhysicalMeter physicalMeter) {
    return authenticatedUser.isSuperAdmin()
           || authenticatedUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
