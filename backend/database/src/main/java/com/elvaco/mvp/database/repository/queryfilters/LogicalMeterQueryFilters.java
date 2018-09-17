package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.alarmQueryFilter;
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

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, parameters, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
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
      case REPORTED:
        return parameters.getPeriod()
          .map(selectionPeriod -> meterStatusQueryFilter(selectionPeriod, toStatusTypes(values)))
          .orElse(null);
      case ALARM:
        return parameters.getPeriod()
          .map(selectionPeriod -> alarmQueryFilter(values))
          .orElse(null);
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
          .or(LOGICAL_METER.location.city.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.location.streetAddress.startsWithIgnoreCase(str))
          .or(PHYSICAL_METER.manufacturer.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.meterDefinition.medium.startsWithIgnoreCase(str));
      default:
        return null;
    }
  }
}
