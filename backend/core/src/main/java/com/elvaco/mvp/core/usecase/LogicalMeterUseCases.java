package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.OrganisationFilter.parametersWithOrganisationId;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LogicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final LogicalMeters logicalMeters;
  private final Measurements measurements;

  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeters.findAllBy(parametersWithOrganisationId(currentUser, parameters));
  }

  public List<LogicalMeter> findAllWithStatuses(RequestParameters parameters) {
    return logicalMeters.findAllWithStatuses(parametersWithOrganisationId(
      currentUser,
      parameters
    ));
  }

  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAll(
      parametersWithOrganisationId(currentUser, parameters),
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

  public Optional<LogicalMeter> findBy(RequestParameters parameters) {
    Optional<LogicalMeter> meter = logicalMeters.findBy(parametersWithOrganisationId(
      currentUser,
      parameters
    ));

    return parameters.getAsZonedDateTime(BEFORE)
      .map(beforeTime -> meter.map(m -> withLatestReadouts(m, beforeTime)))
      .orElse(meter);
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
    return logicalMeters.summary(parametersWithOrganisationId(
      currentUser,
      parameters
    ));
  }

  public List<LogicalMeter> selectionTree(RequestParameters parameters) {
    return logicalMeters.findAllForSelectionTree(parametersWithOrganisationId(
      currentUser,
      parameters
    ));
  }

  private LogicalMeter withLatestReadouts(LogicalMeter logicalMeter, ZonedDateTime before) {
    return logicalMeter.activePhysicalMeter()
      .map(pm -> pm.id)
      .map(id -> findLatestMeasurements(logicalMeter, before, id))
      .map(logicalMeter::withMeasurements)
      .orElse(logicalMeter);
  }

  private List<Measurement> findLatestMeasurements(
    LogicalMeter logicalMeter,
    ZonedDateTime before,
    UUID physicalMeterId
  ) {
    return logicalMeter.getQuantities().stream()
      .map(quantity -> measurements.findLatestReadout(physicalMeterId, before, quantity)
        .orElse(null))
      .filter(Objects::nonNull)
      .collect(toList());
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
