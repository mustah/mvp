package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.util.LogicalMeterHelper;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LogicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final LogicalMeters logicalMeters;
  private final Measurements measurements;

  public List<LogicalMeter> findAll(RequestParameters parameters) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters))
      .stream()
      .map(logicalMeter -> withCollectionPercentage(logicalMeter, parameters))
      .collect(toList());
  }

  public Page<LogicalMeter> findAllWithMeasurements(
    RequestParameters parameters,
    Pageable pageable
  ) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters), pageable)
      .map(this::withLatestReadouts);
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized(
      "User '" + currentUser.getUsername() + "' is not allowed to create this meter."
    );
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  public Optional<LogicalMeter> findByIdWithMeasurements(UUID id) {
    return findById(id).map(this::withLatestReadouts);
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
    return logicalMeters.summary(setCurrentUsersOrganisationId(
      currentUser,
      parameters
    ));
  }

  private LogicalMeter withLatestReadouts(LogicalMeter logicalMeter) {
    if (!logicalMeter.activePhysicalMeter().isPresent()) {
      return logicalMeter;
    }

    List<Measurement> latestMeasurements = new ArrayList<>();
    UUID physicalMeterId = logicalMeter.activePhysicalMeter().get().id;

    for (Quantity quantity : logicalMeter.getQuantities()) {
      measurements.findLatestReadout(physicalMeterId, quantity).ifPresent(latestMeasurements::add);
    }

    return logicalMeter.withMeasurements(latestMeasurements);
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
