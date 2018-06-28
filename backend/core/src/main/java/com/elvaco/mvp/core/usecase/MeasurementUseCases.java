package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Measurements;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public Optional<Measurement> findById(Long id) {
    return measurements.findById(id)
      .filter(m -> currentUser.isSuperAdmin() || isWithinOrganisation(m.physicalMeter));
  }

  public Optional<Measurement> findBy(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  ) {
    return measurements.findBy(physicalMeterId, created, quantity);
  }

  public Collection<Measurement> save(Collection<Measurement> measurementsCollection) {
    return measurements.save(measurementsCollection);
  }

  public List<MeasurementValue> averageForPeriod(
    List<UUID> meterIds,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurements.findAverageForPeriod(meterIds, seriesQuantity, from, to, resolution);
  }

  public List<MeasurementValue> seriesForPeriod(
    UUID meterId,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurements.findSeriesForPeriod(meterId, seriesQuantity, from, to, resolution);
  }

  private boolean isWithinOrganisation(PhysicalMeter physicalMeter) {
    return physicalMeter != null && currentUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
