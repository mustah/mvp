package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.meterStatusQueryFilter;
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

  private ZonedDateTime before;
  private ZonedDateTime after;
  private List<StatusType> statuses;

  @Override
  public Optional<Predicate> buildPredicateFor(RequestParameter parameter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(RequestParameter parameter, List<String> values) {
    switch (parameter) {
      case ID:
        return LOGICAL_METER.id.in(toUuids(values));
      case ORGANISATION:
        return LOGICAL_METER.organisationId.in(toUuids(values));
      case MEDIUM:
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case FACILITY:
        return LOGICAL_METER.externalId.in(values);
      case CITY:
        return LocationExpressions.whereCity(toCityParameters(values));
      case ADDRESS:
        return LocationExpressions.whereAddress(toAddressParameters(values));
      case BEFORE:
        before = getZonedDateTimeFrom(values);
        return meterStatusQueryFilter(after, before, statuses);
      case AFTER:
        after = getZonedDateTimeFrom(values);
        return meterStatusQueryFilter(after, before, statuses);
      case STATUS:
      case METER_STATUS:
        statuses = toStatusTypes(values);
        return meterStatusQueryFilter(after, before, statuses);
      case GATEWAY_SERIAL:
        return GATEWAY.serial.in(values);
      case MANUFACTURER:
        return PHYSICAL_METER.manufacturer.in(values);
      case SECONDARY_ADDRESS:
        return PHYSICAL_METER.address.in(values);
      case WILDCARD:
        String str = values.get(0);
        return LOGICAL_METER.externalId.startsWithIgnoreCase(str)
          .or(PHYSICAL_METER.address.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.location.city.startsWith(str))
          .or(LOGICAL_METER.location.streetAddress.startsWith(str))
          .or(PHYSICAL_METER.manufacturer.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.meterDefinition.medium.startsWithIgnoreCase(str));
      default:
        return null;
    }
  }
}
