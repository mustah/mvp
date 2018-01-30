package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public MeasurementUseCases(AuthenticatedUser currentUser, Measurements measurements) {
    this.currentUser = currentUser;
    this.measurements = measurements;
  }

  public Page<Measurement> findAllScaled(
    String scale, Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return measurements.findAllScaled(
      scale,
      complementFilterParameters(filterParams),
      pageable
    );
  }

  public Page<Measurement> findAll(
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return measurements.findAll(
      complementFilterParameters(filterParams),
      pageable
    );
  }

  public Optional<Measurement> findById(Long id) {
    Measurement measurement = measurements.findOne(id);
    if (currentUser.isSuperAdmin()
      || currentUser.isWithinOrganisation(measurement.physicalMeter.organisation)) {
      return Optional.of(measurement);
    }
    return Optional.empty();
  }

  private Map<String, List<String>> complementFilterParameters(
    Map<String, List<String>>
      filterParams
  ) {
    if (!currentUser.isSuperAdmin()) {
      Long organisationId = currentUser.getOrganisation().id;
      filterParams.put("organisation", Collections.singletonList(organisationId.toString()));
    }
    return filterParams;
  }
}
