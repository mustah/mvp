package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;

public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public MeasurementUseCases(AuthenticatedUser currentUser, Measurements measurements) {
    this.currentUser = currentUser;
    this.measurements = measurements;
  }

  public List<Measurement> findAll(String scale, RequestParameters parameters) {
    if (scale != null) {
      return measurements.findAllByScale(
        scale,
        setCurrentUsersOrganisationId(currentUser, parameters)
      );
    } else {
      return measurements.findAll(setCurrentUsersOrganisationId(currentUser, parameters));
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

  public List<MeasurementValue> averageForPeriod(
    List<UUID> meterIds,
    String quantity,
    String unit,
    ZonedDateTime from,
    ZonedDateTime to,
    String resolution
  ) {
    return measurements.getAverageForPeriod(meterIds, quantity, unit, from, to, resolution);
  }

  private boolean isWithinOrganisation(PhysicalMeter physicalMeter) {
    return physicalMeter != null && currentUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
