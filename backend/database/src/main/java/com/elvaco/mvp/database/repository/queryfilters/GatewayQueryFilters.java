package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.gatewayStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.meterStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity GATEWAY = gatewayEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private ZonedDateTime before;
  private ZonedDateTime after;
  private List<StatusType> statuses;

  @Override
  public Optional<Predicate> buildPredicateFor(String parameterName, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameterName, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return GATEWAY.id.in(toUuids(values));
      case "organisation":
        return GATEWAY.organisationId.in(toUuids(values));
      case "gatewaySerial":
        return GATEWAY.serial.in(values);
      case "containsGatewaySerial":
        return GATEWAY.serial.contains(values.get(0).toLowerCase());
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
      case "status":
      case "gatewayStatus":
        statuses = toStatusTypes(values);
        return gatewayStatusQueryFilter(after, before, statuses);
      case "meterStatus":
        statuses = toStatusTypes(values);
        return meterStatusQueryFilter(after, before, statuses);
      case "facility":
        return LOGICAL_METER.externalId.in(values);
      case "medium":
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case "manufacturer":
        return LOGICAL_METER.physicalMeters.any().manufacturer.in(values);
      case "secondaryAddress":
        return LOGICAL_METER.physicalMeters.any().address.in(values);
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

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(GATEWAY.meters.any().location);
  }
}
