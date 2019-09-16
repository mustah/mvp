package com.elvaco.mvp.core.usecase;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
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
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private static final long MAX_METERS_DAYS_FOR_ALL = 10;

  private final AuthenticatedUser currentUser;
  private final Measurements measurements;
  private final LogicalMeters logicalMeters;

  public void createOrUpdate(Measurement measurement, LogicalMeter logicalMeter) {
    measurements.createOrUpdate(measurement, logicalMeter);
  }

  public Map<String, List<MeasurementValue>> findAverageForPeriod(
    MeasurementParameter parameter
  ) {
    if (TemporalResolution.all == parameter.getResolution()) {
      if (limitMeasurementsForAll(parameter)) {
        throw newInvalidMeasurementRequestScope();
      }
      return measurements.findAverageAllForPeriod(enforceOrganisationFilters(parameter));
    } else {
      return measurements.findAverageForPeriod(enforceOrganisationFilters(parameter));
    }
  }

  public Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(
    MeasurementParameter parameter
  ) {
    if (TemporalResolution.all == parameter.getResolution()) {
      if (limitMeasurementsForAll(parameter)) {
        throw newInvalidMeasurementRequestScope();
      }
      return measurements.findAllForPeriod(enforceOrganisationFilters(parameter));
    } else {
      return measurements.findSeriesForPeriod(enforceOrganisationFilters(parameter));
    }
  }

  public Map<String, QuantityParameter> getPreferredQuantityParameters(
    RequestParameters parameters
  ) {
    return logicalMeters.getPreferredQuantityParameters(parameters).stream()
      .collect(toMap(qp -> qp.name, Function.identity()));
  }

  private MeasurementParameter enforceOrganisationFilters(MeasurementParameter parameter) {
    Set<UUID> organisationIds = Optional.of(currentUser)
      .filter(AuthenticatedUser::isSuperAdmin)
      .map(__ -> logicalMeters.findAllBy(parameter.getParameters()).stream()
        .map(lm -> lm.organisationId)
        .collect(toSet()))
      .orElseGet(Set::of);
    return parameter.toBuilder().parameters(parameter.getParameters()
      .setAllIds(ORGANISATION, organisationIds)
      .ensureOrganisationFilters(currentUser))
      .build();
  }

  private static InvalidMeasumentRequestScope newInvalidMeasurementRequestScope() {
    return new InvalidMeasumentRequestScope(
      "Scope of period length and meters is too large for this resolution");
  }

  private static boolean limitMeasurementsForAll(MeasurementParameter parameter) {
    long days = parameter.getParameters().getReportPeriod()
      .map(p -> ChronoUnit.DAYS.between(p.start, p.stop))
      .orElse(MAX_METERS_DAYS_FOR_ALL);
    long meters = parameter.getParameters().getValues(LOGICAL_METER_ID).size();

    return days * meters > MAX_METERS_DAYS_FOR_ALL;
  }
}
