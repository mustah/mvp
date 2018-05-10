package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;
import static java.util.stream.Collectors.toList;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;
  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  ZonedDateTime before;
  ZonedDateTime after;
  List<StatusType> statuses;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return Q.id.in(mapValues(UUID::fromString, values));
      case "medium":
        return Q.meterDefinition.medium.in(values);
      case "manufacturer":
        return Q.physicalMeters.any().manufacturer.in(values);
      case "organisation":
        return Q.organisationId.in(mapValues(UUID::fromString, values));
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      case "before":
        before = ZonedDateTime.parse(values.get(0));
        return statusQueryFilter(after, before, statuses);
      case "after":
        after = ZonedDateTime.parse(values.get(0));
        return statusQueryFilter(after, before, statuses);
      case "status":
        statuses = values.stream().map(StatusType::from).collect(toList());
        return statusQueryFilter(after, before, statuses);
      default:
        return null;
    }
  }

  @Nullable
  private Predicate statusQueryFilter(
    ZonedDateTime periodStart,
    ZonedDateTime periodStop,
    List<StatusType> statuses
  ) {
    if (periodStart == null || periodStop == null || statuses == null || statuses.isEmpty()) {
      return null;
    }

    return (STATUS_LOG.stop.isNull().or(STATUS_LOG.stop.after(periodStart)))
      .and(
        STATUS_LOG.start.before(periodStop)
      ).and(STATUS_LOG.status.in(statuses));
  }

  @Nullable
  private Predicate whereCity(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.countriesAndCities(parameters))
      .or(locationExpressions.unknownCities(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private Predicate whereAddress(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.address(parameters))
      .or(locationExpressions.unknownAddress(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(Q.location);
  }
}
