package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.gatewayStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.meterStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity GATEWAY = gatewayEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

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
        return LocationExpressions.whereCity(toCityParameters(values));
      case "address":
        return LocationExpressions.whereAddress(toAddressParameters(values));
      case "before":
        before = getZonedDateTimeFrom(values);
        return gatewayStatusQueryFilter(after, before, statuses);
      case "after":
        after = getZonedDateTimeFrom(values);
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
        return PHYSICAL_METER.manufacturer.in(values);
      case "secondaryAddress":
        return PHYSICAL_METER.address.in(values);
      default:
        return null;
    }
  }
}
