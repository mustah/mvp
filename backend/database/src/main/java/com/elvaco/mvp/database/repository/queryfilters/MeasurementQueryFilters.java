package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;
import static java.util.stream.Collectors.toList;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;
  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
  ZonedDateTime before;
  ZonedDateTime after;
  List<StatusType> statuses;

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "physicalMeterId":
        return Q.physicalMeter.id.in(mapValues(UUID::fromString, values));
      case "id":
        return Q.physicalMeter.logicalMeterId.in(mapValues(UUID::fromString, values));
      case "medium":
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case "manufacturer":
        return Q.physicalMeter.manufacturer.in(values);
      case "organisation":
        return Q.physicalMeter.organisation.id.in(mapValues(UUID::fromString, values));
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      case "before": {
        before = ZonedDateTime.parse(values.get(0));
        Predicate statusPredicate = statusQueryFilter(after, before, statuses);
        BooleanExpression beforePredicate = Q.created.lt(before);
        if (statusPredicate == null) {
          return beforePredicate;
        } else {
          return beforePredicate.and(statusPredicate);
        }
      }
      case "after": {
        after = ZonedDateTime.parse(values.get(0));
        Predicate statusPredicate = statusQueryFilter(after, before, statuses);
        BooleanExpression afterPredicate = Q.created.goe(after);
        if (statusPredicate == null) {
          return afterPredicate;
        } else {
          return afterPredicate.and(statusPredicate);
        }
      }
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
  private Predicate whereCity(LocationParametersParser.Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.countriesAndCities(parameters))
      .or(locationExpressions.unknownCities(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private Predicate whereAddress(LocationParametersParser.Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.address(parameters))
      .or(locationExpressions.unknownAddress(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(LOGICAL_METER.location);
  }
}
