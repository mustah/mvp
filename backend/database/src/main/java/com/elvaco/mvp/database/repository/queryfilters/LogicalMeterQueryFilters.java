package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  @Override
  public Optional<Predicate> prePredicateHook(RequestParameters parameters) {
    if (parameters.hasName(AFTER) && parameters.hasName(BEFORE)) {
      return Optional.ofNullable(new BooleanBuilder().and(periodQueryFilter(
        parameters.getFirst(AFTER),
        parameters.getFirst(BEFORE)
      )).getValue());
    }
    return Optional.empty();
  }

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
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
      case "status":
        return Q.physicalMeters.any().statusLogs.any().status.name.in(values);
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      default:
        return null;
    }
  }

  private Predicate periodQueryFilter(String after, String before) {
    ZonedDateTime start = ZonedDateTime.parse(after);
    ZonedDateTime stop = ZonedDateTime.parse(before);
    return isBefore(stop).and(isAfter(start).or(hasNoEndDate()));
  }

  private BooleanExpression hasNoEndDate() {
    return Q.physicalMeters.any().statusLogs.any().stop.isNull();
  }

  private BooleanExpression isAfter(ZonedDateTime start) {
    return Q.physicalMeters.any().statusLogs.any().stop.after(start);
  }

  private BooleanExpression isBefore(ZonedDateTime stop) {
    return Q.physicalMeters.any().statusLogs.any().start.before(stop);
  }

  @Nullable
  private Predicate whereCity(Parameters parameters) {
    if (parameters.hasCities()) {
      return Q.location.country.toLowerCase().in(parameters.countries)
        .and(Q.location.city.toLowerCase().in(parameters.cities));
    }
    return null;
  }

  @Nullable
  private Predicate whereAddress(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return Q.location.country.toLowerCase().in(parameters.countries)
        .and(Q.location.city.toLowerCase().in(parameters.cities))
        .and(Q.location.streetAddress.toLowerCase().in(parameters.addresses));
    }
    return null;
  }
}
