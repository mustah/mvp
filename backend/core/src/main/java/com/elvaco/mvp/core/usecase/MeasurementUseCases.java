package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public void createOrUpdate(Measurement m) {
    measurements.createOrUpdate(
      m.physicalMeter,
      m.created,
      m.quantity,
      m.unit,
      m.value
    );
  }

  public List<Measurement> findAll(RequestParameters parameters) {
    return measurements.findAll(parameters.ensureOrganisationFilters(currentUser));
  }

  public List<MeasurementValue> findAverageForPeriod(MeasurementParameter parameter) {
    return measurements.findAverageForPeriod(parameter);
  }

  public Map<UUID, List<MeasurementValue>> findSeriesForPeriod(MeasurementParameter parameter) {
    return measurements.findSeriesForPeriod(parameter);
  }
}
