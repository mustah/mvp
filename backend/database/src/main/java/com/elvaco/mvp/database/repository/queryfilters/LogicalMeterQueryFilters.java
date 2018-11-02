package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.alarmQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.meterStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class LogicalMeterQueryFilters extends QueryFilters {

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
      case LOGICAL_METER_ID:
        return LOGICAL_METER.id.in(toUuids(values));
      case ORGANISATION:
        return LOGICAL_METER.organisationId.in(toUuids(values));
      case MEDIUM:
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case FACILITY:
        return LOGICAL_METER.externalId.in(values);
      case CITY:
        return LocationPredicates.whereCityOrUnknown(values);
      case ADDRESS:
        return LocationPredicates.whereAddressOrUnknown(values);
      case REPORTED:
        return parameters.getPeriod()
          .map(selectionPeriod -> meterStatusQueryFilter(selectionPeriod, toStatusTypes(values)))
          .orElse(null);
      case ALARM:
        return parameters.getPeriod()
          .map(selectionPeriod -> alarmQueryFilter(values))
          .orElse(null);
      case GATEWAY_ID:
        return GATEWAY.id.eq(UUID.fromString(values.get(0)));
      case GATEWAY_SERIAL:
        return GATEWAY.serial.in(values);
      case MANUFACTURER:
        return PHYSICAL_METER.manufacturer.in(values);
      case SECONDARY_ADDRESS:
        return PHYSICAL_METER.address.in(values);
      case WILDCARD:
        String str = values.get(0);
        return LOGICAL_METER.externalId.startsWithIgnoreCase(str)
          .or(LOGICAL_METER.meterDefinition.medium.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.location.city.startsWithIgnoreCase(str))
          .or(LOGICAL_METER.location.streetAddress.startsWithIgnoreCase(str))
          .or(PHYSICAL_METER.manufacturer.startsWithIgnoreCase(str))
          .or(PHYSICAL_METER.address.startsWithIgnoreCase(str));
      default:
        return null;
    }
  }
}
