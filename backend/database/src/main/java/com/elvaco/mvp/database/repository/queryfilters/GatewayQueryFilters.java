package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity GATEWAY = QGatewayEntity.gatewayEntity;
  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG = gatewayStatusLogEntity;

  private ZonedDateTime before;
  private ZonedDateTime after;
  private List<StatusType> statuses;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return GATEWAY.id.in(toUuids(values));
      case "organisation":
        return GATEWAY.organisationId.in(toUuids(values));
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      case "before":
        before = ZonedDateTime.parse(values.get(0));
        return gatewayStatusQueryFilter(after, before, statuses);
      case "after":
        after = ZonedDateTime.parse(values.get(0));
        return gatewayStatusQueryFilter(after, before, statuses);
      case "gatewayStatus":
        statuses = toStatusTypes(values);
        return gatewayStatusQueryFilter(after, before, statuses);
      case "gatewaySerial":
        return GATEWAY.serial.in(values);
      default:
        return null;
    }
  }

  @Nullable
  private static Predicate whereCity(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.countriesAndCities(parameters))
      .or(locationExpressions.unknownCities(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private static Predicate whereAddress(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.address(parameters))
      .or(locationExpressions.unknownAddress(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private static Predicate gatewayStatusQueryFilter(
    ZonedDateTime start,
    ZonedDateTime stop,
    List<StatusType> statuses
  ) {
    if (start == null || stop == null || statuses == null || statuses.isEmpty()) {
      return null;
    }
    return (GATEWAY_STATUS_LOG.stop.isNull().or(GATEWAY_STATUS_LOG.stop.after(start)))
      .and(GATEWAY_STATUS_LOG.start.before(stop))
      .and(GATEWAY_STATUS_LOG.status.in(statuses));
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(GATEWAY.meters.any().location);
  }
}
