package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.security.MvpPrincipal;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public class MeasurementUseCases {
  private final MvpPrincipal principal;
  private final Measurements measurements;

  public MeasurementUseCases(MvpPrincipal principal, Measurements measurements) {
    this.principal = principal;
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

  public Measurement findById(Long id) {
    Measurement measurement = measurements.findOne(id);
    if (principal.isSuperAdmin()
      || principal.isWithinOrganisation(measurement.physicalMeter.organisation)) {
      return measurement;
    }
    return null;
  }

  private Map<String, List<String>> complementFilterParameters(
    Map<String, List<String>>
      filterParams
  ) {
    if (!principal.isSuperAdmin()) {
      Long organisationId = principal.getOrganisationId();
      filterParams.put("organisation", Collections.singletonList(organisationId.toString()));
    }
    return filterParams;
  }
}
