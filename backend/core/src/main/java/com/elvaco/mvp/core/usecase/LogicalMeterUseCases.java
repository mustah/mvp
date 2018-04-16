package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.util.LogicalMeterHelper;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;
import static java.util.stream.Collectors.toList;

public class LogicalMeterUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public LogicalMeterUseCases(
    AuthenticatedUser currentUser,
    LogicalMeters logicalMeters,
    Measurements measurements
  ) {
    this.currentUser = currentUser;
    this.logicalMeters = logicalMeters;
    this.measurements = measurements;
  }

  public List<LogicalMeter> findAll(RequestParameters parameters) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters))
      .stream()
      .map(logicalMeter -> withCollectionPercentage(logicalMeter, parameters))
      .collect(toList());
  }

  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters), pageable)
      .map(logicalMeter -> withCollectionPercentage(logicalMeter, parameters));
  }

  public Page<LogicalMeter> findAllWithMeasurements(
    RequestParameters parameters,
    Pageable pageable
  ) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters), pageable)
      .map(logicalMeter -> withCollectionPercentage(logicalMeter, parameters))
      .map(logicalMeter -> {
        if (!logicalMeter.activePhysicalMeter().isPresent()) {
          return logicalMeter;
        }
        return logicalMeter.withMeasurements(measurements.findLatestValues(
          logicalMeter.activePhysicalMeter().get().id
        ));
      });
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to "
      + "create this meter.");
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  public Optional<LogicalMeter> findByIdWithMeasurements(UUID id) {
    return findById(id).map(logicalMeter -> {
      if (!logicalMeter.activePhysicalMeter().isPresent()) {
        return logicalMeter;
      }
      return logicalMeter.withMeasurements(measurements.findLatestValues(
        logicalMeter.activePhysicalMeter().get().id
      ));
    });
  }

  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    if (currentUser.isWithinOrganisation(organisationId) || currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationIdAndExternalId(
        organisationId,
        externalId
      );
    }
    return Optional.empty();
  }

  private LogicalMeter withCollectionPercentage(
    LogicalMeter logicalMeter,
    RequestParameters parameters
  ) {
    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return logicalMeter;
    }

    return logicalMeter.withCollectionPercentage(
      LogicalMeterHelper.getCollectionPercent(
        logicalMeter.physicalMeters,
        ZonedDateTime.parse(parameters.getFirst("after")),
        ZonedDateTime.parse(parameters.getFirst("before")),
        logicalMeter.meterDefinition.quantities.size()
      ).getCollectionPercentage()
    );
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
