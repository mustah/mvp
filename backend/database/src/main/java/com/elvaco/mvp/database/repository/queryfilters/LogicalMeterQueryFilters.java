package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toDate;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  private static final Map<String, Function<String, Predicate>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(UUID.fromString(id)));

    FILTERABLE_PROPERTIES.put("medium", Q.meterDefinition.medium::eq);

    FILTERABLE_PROPERTIES.put("manufacturer", Q.physicalMeters.any().manufacturer::eq);

    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String id) -> Q.organisationId.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, Predicate>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Nullable
  @Override
  public Predicate toExpression(RequestParameters parameters) {
    BooleanBuilder builder = new BooleanBuilder();
    if (parameters.hasName(AFTER) && parameters.hasName(BEFORE)) {
      builder.and(periodQueryFilter(parameters));
    }
    return builder
      .and(whereStatusesIn(parameters.getValues("status")))
      .and(whereCity(toCityParameters(parameters.getValues("city"))))
      .and(whereAddress(toAddressParameters(parameters.getValues("address"))))
      .and(propertiesExpression(parameters))
      .getValue();
  }

  private Predicate periodQueryFilter(RequestParameters parameters) {
    Date start = toDate(parameters.getFirst(AFTER));
    Date stop = toDate(parameters.getFirst(BEFORE));
    return isBefore(stop).and(isAfter(start).or(hasNoEndDate()));
  }

  private BooleanExpression hasNoEndDate() {
    return Q.physicalMeters.any().statusLogs.any().stop.isNull();
  }

  private BooleanExpression isAfter(Date start) {
    return Q.physicalMeters.any().statusLogs.any().stop.after(start);
  }

  private BooleanExpression isBefore(Date stop) {
    return Q.physicalMeters.any().statusLogs.any().start.before(stop);
  }

  private Predicate whereStatusesIn(List<String> statuses) {
    if (!statuses.isEmpty()) {
      return Q.physicalMeters.any().statusLogs.any().status.name.in(statuses);
    }
    return null;
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
