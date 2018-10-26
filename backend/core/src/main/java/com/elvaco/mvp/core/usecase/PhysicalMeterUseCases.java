package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhysicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;

  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter.organisation.id)) {
      return physicalMeters.save(physicalMeter);
    }
    throw userIsUnauthorized(physicalMeter.id);
  }

  public PhysicalMeter saveWithStatuses(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter.organisation.id)) {
      physicalMeters.save(physicalMeter);
      meterStatusLogs.save(physicalMeter.statuses);
      return physicalMeter;
    }
    throw userIsUnauthorized(physicalMeter.id);
  }

  public Optional<PhysicalMeter> findByWithStatuses(
    UUID organisationId,
    String externalId,
    String address
  ) {
    if (hasTenantAccess(organisationId)) {
      return physicalMeters.findByWithStatuses(organisationId, externalId, address);
    }
    return Optional.empty();
  }

  public Optional<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId,
    String address
  ) {
    if (hasTenantAccess(organisationId)) {
      return physicalMeters.findBy(organisationId, externalId, address);
    }
    return Optional.empty();
  }

  private Unauthorized userIsUnauthorized(UUID id) {
    return new Unauthorized(String.format(
      "User '%s' is not allowed to update physical meter with ID %s",
      currentUser.getUsername(),
      id
    ));
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
