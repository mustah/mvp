package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class LocationQueryFilters extends QueryFilters {

  private static final QLocationEntity LOCATION = QLocationEntity.locationEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "city":
        return LOCATION.city.contains(values.get(0).toLowerCase());
      case "address":
        return LOCATION.streetAddress.contains(values.get(0).toLowerCase());
      case "organisation":
        return LOGICAL_METER.organisationId.in(toUuids(values));
      default:
        return null;
    }
  }
}
