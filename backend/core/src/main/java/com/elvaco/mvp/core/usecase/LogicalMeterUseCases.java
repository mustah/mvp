package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static com.elvaco.mvp.core.security.OrganisationFilter.addOrganisationIdToFilterParams;

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

  public Optional<LogicalMeter>  findById(Long id) {
    return logicalMeters.findById(id);
  }

  public List<LogicalMeter> findAll() {
    return logicalMeters.findAll();
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
    return logicalMeters.save(logicalMeter);
  }

  public List<Measurement> measurements(LogicalMeter logicalMeter) {
    if (logicalMeter.physicalMeters.isEmpty() || logicalMeter.getQuantities().isEmpty()) {
      return Collections.emptyList();
    }

    Map<String, List<String>> filter = new HashMap<>();
    filter.put(
      "meterId",
      logicalMeter.physicalMeters.stream()
        .filter(m -> m.id != null)
        .map(m -> m.id.toString())
        .collect(Collectors.toList())
    );

    filter.put(
      "quantity",
      logicalMeter.getQuantities().stream()
        .map(Quantity::getName).collect(Collectors.toList())
    );
    return measurements.findAll(filter);
  }
}
