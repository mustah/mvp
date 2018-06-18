package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QGatewayEntity GATEWAY =
    QGatewayEntity.gatewayEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

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
        return LOGICAL_METER.id.in(toUuids(values));
      case "medium":
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case "manufacturer":
        return LOGICAL_METER.physicalMeters.any().manufacturer.in(values);
      case "organisation":
        return LOGICAL_METER.organisationId.in(toUuids(values));
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      case "before":
        before = ZonedDateTime.parse(values.get(0));
        return meterStatusQueryFilter(after, before, statuses);
      case "after":
        after = ZonedDateTime.parse(values.get(0));
        return meterStatusQueryFilter(after, before, statuses);
      case "status":
        statuses = toStatusTypes(values);
        return meterStatusQueryFilter(after, before, statuses);
      case "facility":
        return LOGICAL_METER.externalId.in(values);
      case "secondaryAddress":
        return PHYSICAL_METER.address.in(values);
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
  private static Predicate meterStatusQueryFilter(
    ZonedDateTime start,
    ZonedDateTime stop,
    List<StatusType> statuses
  ) {
    if (start == null || stop == null || statuses == null || statuses.isEmpty()) {
      return null;
    }
    return (STATUS_LOG.stop.isNull().or(STATUS_LOG.stop.after(start)))
      .and(STATUS_LOG.start.before(stop))
      .and(STATUS_LOG.status.in(statuses));
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(LOGICAL_METER.location);
  }
}
