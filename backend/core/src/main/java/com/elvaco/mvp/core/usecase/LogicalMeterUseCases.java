package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static com.elvaco.mvp.core.security.OrganisationFilter.addOrganisationIdToFilterParams;
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

  public List<LogicalMeter> findAll() {
    if (!currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationId(currentUser.getOrganisationId());
    } else {
      return logicalMeters.findAll();
    }
  }

  public Page<LogicalMeter> findAll(
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return logicalMeters.findAll(
      addOrganisationIdToFilterParams(currentUser, filterParams),
      pageable
    );
  }

  public List<LogicalMeter> findAll(Map<String, List<String>> filterParams) {
    return logicalMeters.findAll(addOrganisationIdToFilterParams(
      currentUser,
      filterParams
    ));
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to "
                           + "create this meter.");
  }

  public List<Measurement> measurements(LogicalMeter logicalMeter) {
    if (logicalMeter.physicalMeters.isEmpty() || logicalMeter.getQuantities().isEmpty()
      || !hasTenantAccess(logicalMeter)) {
      return Collections.emptyList();
    }

    Map<String, List<String>> filter = new HashMap<>();
    filter.put(
      "meterId",
      logicalMeter.physicalMeters.stream()
        .filter(m -> m.id != null)
        .map(m -> m.id.toString())
        .collect(toList())
    );

    filter.put(
      "quantity",
      logicalMeter.getQuantities().stream()
        .map(quantity -> quantity.name)
        .collect(toList())
    );
    return measurements.findAll(filter);
  }

  public Optional<LogicalMeter> findById(Long id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  private boolean hasTenantAccess(LogicalMeter logicalMeter) {
    return currentUser.isSuperAdmin()
      || logicalMeter.organisationId.equals(currentUser.getOrganisationId());
  }
}
