package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationFilter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

import static java.util.Collections.singletonList;

public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public MeasurementUseCases(AuthenticatedUser currentUser, Measurements measurements) {
    this.currentUser = currentUser;
    this.measurements = measurements;
  }

  public Page<Measurement> findAllPageable(
    String scale,
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    if (scale != null) {
      return findAllByScale(scale, filterParams, pageable);
    } else {
      return findAll(filterParams, pageable);
    }
  }

  public Optional<Measurement> findById(Long id) {
    return measurements.findById(id)
      .flatMap(m -> {
        if (currentUser.isSuperAdmin() || isWithinOrganisation(m.physicalMeter)) {
          return Optional.of(m);
        } else {
          return Optional.empty();
        }
      });
  }

  private Page<Measurement> findAllByScale(
    String scale,
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return measurements.findAllByScale(
      scale,
      OrganisationFilter.complementFilterWithOrganisationParameters(currentUser, filterParams),
      pageable
    );
  }

  private Page<Measurement> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return measurements.findAll(
      OrganisationFilter.complementFilterWithOrganisationParameters(currentUser, filterParams),
      pageable
    );
  }

  private boolean isWithinOrganisation(PhysicalMeter physicalMeter) {
    return physicalMeter != null && currentUser.isWithinOrganisation(physicalMeter.organisation);
  }

  private Map<String, List<String>> complementFilterParameters(
    Map<String, List<String>> filterParams
  ) {
    if (!currentUser.isSuperAdmin()) {
      Long organisationId = currentUser.getOrganisation().id;
      filterParams.put("organisation", singletonList(organisationId.toString()));
    }
    return filterParams;
  }
}
