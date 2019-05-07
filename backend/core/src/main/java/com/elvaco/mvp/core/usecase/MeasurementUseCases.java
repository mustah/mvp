package com.elvaco.mvp.core.usecase;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.exception.InvalidMeasumentRequestScope;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private static final long MAX_METERS_DAYS_FOR_ALL = 10;

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
    if (TemporalResolution.all == parameter.getResolution()) {
      if (limitMeasurementsForAll(parameter)) {
        throw new InvalidMeasumentRequestScope(
          "Scope of period length and meters is too large for this resolution");
      }
      return measurements.findAverageAllForPeriod(parameter.toBuilder()
        .parameters(parameter.getParameters().ensureOrganisationFilters(currentUser))
        .build());
    }
    return measurements.findAverageForPeriod(
      parameter.toBuilder()
        .parameters(parameter.getParameters().ensureOrganisationFilters(currentUser))
        .build()
    );
  }

  public Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(
    MeasurementParameter parameter
  ) {
    if (TemporalResolution.all == parameter.getResolution()) {
      if (limitMeasurementsForAll(parameter)) {
        throw new InvalidMeasumentRequestScope(
          "Scope of period length and meters is too large for this resolution");
      }
      return measurements.findAllForPeriod(parameter.toBuilder()
        .parameters(parameter.getParameters().ensureOrganisationFilters(currentUser))
        .build());
    }

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

  private boolean limitMeasurementsForAll(MeasurementParameter parameter) {
    long days = parameter.getParameters().getReportPeriod()
      .map(p -> ChronoUnit.DAYS.between(p.start, p.stop))
      .orElse(MAX_METERS_DAYS_FOR_ALL);
    long meters = parameter.getParameters().getValues(LOGICAL_METER_ID).size();

    return days * meters > MAX_METERS_DAYS_FOR_ALL;
  }
}
