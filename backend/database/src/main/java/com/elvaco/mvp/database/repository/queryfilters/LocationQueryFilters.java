package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class LocationQueryFilters extends QueryFilters {

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
      case CITY:
        return LocationPredicates.whereCityOrNull(values);
      case Q_CITY:
        return LOCATION.city.contains(values.get(0).toLowerCase());
      case ADDRESS:
        return LocationPredicates.whereAddressOrUnknown(values);
      case Q_ADDRESS:
        return LOCATION.streetAddress.contains(values.get(0).toLowerCase());
      case ORGANISATION:
        return LOGICAL_METER.organisationId.in(toUuids(values));
      default:
        return null;
    }
  }
}
