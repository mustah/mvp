package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Measurements;

public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public MeasurementUseCases(AuthenticatedUser currentUser, Measurements measurements) {
    this.currentUser = currentUser;
    this.measurements = measurements;
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

  public Optional<Measurement> findForMeterCreatedAt(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime when
  ) {
    return measurements.findByPhysicalMeterIdAndQuantityAndCreated(physicalMeterId, quantity, when);
  }

  public Collection<Measurement> save(Collection<Measurement> measurementsCollection) {
    return measurements.save(measurementsCollection);
  }

  public List<MeasurementValue> averageForPeriod(
    List<UUID> meterIds,
    String quantity,
    String unit,
    SeriesDisplayMode seriesDisplayMode,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurements.findAverageForPeriod(meterIds, quantity, unit, seriesDisplayMode, from, to, resolution);
  }

  public List<MeasurementValue> seriesForPeriod(
    UUID meterId,
    String quantity,
    String unit,
    SeriesDisplayMode mode,
    ZonedDateTime from,
    ZonedDateTime to
  ) {
    return measurements.findSeriesForPeriod(meterId, quantity, unit, mode, from, to);
  }

  private boolean isWithinOrganisation(PhysicalMeter physicalMeter) {
    return physicalMeter != null && currentUser.isWithinOrganisation(physicalMeter.organisation.id);
  }
}
