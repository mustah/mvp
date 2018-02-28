package com.elvaco.mvp.core.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static com.elvaco.mvp.core.security.OrganisationFilter.addOrganisationIdToFilterParams;

public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public MeasurementUseCases(AuthenticatedUser currentUser, Measurements measurements) {
    this.currentUser = currentUser;
    this.measurements = measurements;
  }

  public List<Measurement> findAll(
    String scale,
    Map<String, List<String>> filterParams
  ) {
    if (scale != null) {
      return measurements.findAllByScale(
        scale,
        addOrganisationIdToFilterParams(currentUser, filterParams)
      );
    } else {
      return measurements.findAll(
        addOrganisationIdToFilterParams(currentUser, filterParams)
      );
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

  public Collection<Measurement> save(Collection<Measurement> measurementsCollection) {
    return measurements.save(measurementsCollection);

  }

  private boolean isWithinOrganisation(PhysicalMeter physicalMeter) {
    return physicalMeter != null && currentUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
