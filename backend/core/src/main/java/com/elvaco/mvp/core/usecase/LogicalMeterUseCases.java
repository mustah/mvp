package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final LogicalMeters logicalMeters;

  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeters.findAllBy(parameters.ensureOrganisation(currentUser));
  }

  public List<LogicalMeter> findAllWithDetails(RequestParameters parameters) {
    return logicalMeters.findAllWithDetails(parameters.ensureOrganisation(currentUser));
  }

  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAll(parameters.ensureOrganisation(currentUser), pageable);
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized(
      "User '" + currentUser.getUsername() + "' is not allowed to create this meter."
    );
  }

  public Optional<LogicalMeter> findBy(UUID organisationId, String externalId) {
    if (currentUser.isWithinOrganisation(organisationId) || currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationIdAndExternalId(organisationId, externalId);
    }
    return Optional.empty();
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  public Optional<LogicalMeter> deleteById(UUID id) {
    if (!currentUser.isSuperAdmin()) {
      throw new Unauthorized(
        "User '" + currentUser.getUsername() + "' is not allowed to delete this meter."
      );
    }
    Optional<LogicalMeter> logicalMeter = findById(id);
    logicalMeter.ifPresent(logicalMeters::delete);

    return logicalMeter;
  }

  public MeterSummary summary(RequestParameters parameters) {
    return logicalMeters.summary(parameters.ensureOrganisation(currentUser));
  }

  public List<LogicalMeter> selectionTree(RequestParameters parameters) {
    return logicalMeters.findAllForSelectionTree(parameters.ensureOrganisation(currentUser));
  }

  public Optional<UUID> effectiveOrganisationId(UUID logicalMeterId) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(logicalMeterId).map(lm -> lm.organisationId);
    } else {
      return Optional.of(currentUser.getOrganisationId());
    }
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
