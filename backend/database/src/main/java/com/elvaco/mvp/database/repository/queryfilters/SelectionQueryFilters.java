package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class SelectionQueryFilters extends QueryFilters {

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(RequestParameter parameter, List<String> values) {
    switch (parameter) {
      case ORGANISATION:
        return LOGICAL_METER.pk.organisationId.in(toUuids(values));
      case CITY:
        return LocationPredicates.whereCityOrNull(values);
      case ADDRESS:
        return LocationPredicates.whereAddressOrNull(values);
      case FACILITY:
        return LOGICAL_METER.externalId.in(values);
      case SECONDARY_ADDRESS:
        return PHYSICAL_METER.address.in(values);
      case Q_FACILITY:
        return LOGICAL_METER.externalId.containsIgnoreCase(values.get(0));
      case Q_SECONDARY_ADDRESS:
        return PHYSICAL_METER.address.containsIgnoreCase(values.get(0));
      default:
        return null;
    }
  }
}
