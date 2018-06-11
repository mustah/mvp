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
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;

@RequiredArgsConstructor
public class LogicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final LogicalMeters logicalMeters;
  private final Measurements measurements;

  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeters.findAllBy(setCurrentUsersOrganisationId(
      currentUser,
      parameters
    ));
  }

  public List<LogicalMeter> findAll(RequestParameters parameters) {
    return logicalMeters.findAllWithStatuses(setCurrentUsersOrganisationId(
      currentUser,
      parameters
    ));
  }

  public Page<LogicalMeter> findAll(
    RequestParameters parameters,
    Pageable pageable
  ) {
    return logicalMeters.findAllWithStatuses(
      setCurrentUsersOrganisationId(currentUser, parameters),
      pageable
    );
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized(
      "User '" + currentUser.getUsername() + "' is not allowed to create this meter."
    );
  }

  public Optional<LogicalMeter> findOneBy(RequestParameters parameters) {
    Optional<LogicalMeter> meter = logicalMeters.findOneBy(setCurrentUsersOrganisationId(
      currentUser,
      parameters
    ));

    return parameters.getAsZonedDateTime("before")
      .map(beforeTime -> meter.map(m -> withLatestReadouts(m, beforeTime)))
      .orElse(meter);
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
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

  private LogicalMeter withLatestReadouts(LogicalMeter logicalMeter, ZonedDateTime before) {
    if (!logicalMeter.activePhysicalMeter().isPresent()) {
      return logicalMeter;
    }

    List<Measurement> latestMeasurements = new ArrayList<>();
    UUID physicalMeterId = logicalMeter.activePhysicalMeter().get().id;

    for (Quantity quantity : logicalMeter.getQuantities()) {
      measurements.findLatestReadout(physicalMeterId, before, quantity)
        .ifPresent(latestMeasurements::add);
    }

    return logicalMeter.withMeasurements(latestMeasurements);
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
