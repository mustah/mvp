package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;
  private final LogicalMeters logicalMeters;

  public void createOrUpdate(Measurement m) {
    measurements.createOrUpdate(m);
  }

  public List<Measurement> findAll(RequestParameters parameters) {
    return measurements.findAll(parameters.ensureOrganisationFilters(currentUser));
  }

  public Map<String, List<MeasurementValue>> findAverageForPeriod(
    MeasurementParameter parameter
  ) {
    return measurements.findAverageForPeriod(
      parameter.toBuilder()
        .parameters(parameter.getParameters().ensureOrganisationFilters(currentUser))
        .build()
    );
  }

  public Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(
    MeasurementParameter parameter
  ) {
    return measurements.findSeriesForPeriod(parameter.toBuilder()
      .parameters(parameter.getParameters().ensureOrganisationFilters(currentUser))
      .build());
  }

  public Map<String, QuantityParameter> getPreferredQuantityParameters(
    RequestParameters parameters
  ) {
    return logicalMeters.getPreferredQuantityParameters(parameters)
      .stream()
      .collect(toMap(qp -> qp.name, qp -> qp));
  }
}
