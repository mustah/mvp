package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PhysicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;

  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter.organisationId)) {
      return physicalMeters.save(physicalMeter);
    }
    throw userIsUnauthorized(physicalMeter.id);
  }

  public PhysicalMeter saveAndFlush(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter.organisationId)) {
      return physicalMeters.saveAndFlush(physicalMeter);
    }
    throw userIsUnauthorized(physicalMeter.id);
  }

  public PhysicalMeter saveWithStatuses(PhysicalMeter physicalMeter) {
    if (hasTenantAccess(physicalMeter.organisationId)) {
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
    return Optional.of(organisationId)
      .filter(this::hasTenantAccess)
      .flatMap(orgId -> physicalMeters.findByWithStatuses(orgId, externalId, address));
  }

  public Optional<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return Optional.of(organisationId)
      .filter(this::hasTenantAccess)
      .flatMap(orgId -> physicalMeters.findBy(orgId, externalId, address));
  }

  public Optional<PhysicalMeter> getActiveMeterAtTimestamp(
    UUID organisationId,
    String externalId,
    ZonedDateTime newStartTime
  ) {
    return Optional.of(organisationId)
      .filter(this::hasTenantAccess)
      .flatMap(orgId ->
        physicalMeters.findBy(orgId, externalId)
          .stream()
          .filter(p -> p.activePeriod.contains(newStartTime))
          .findFirst());
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
