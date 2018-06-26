package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class PhysicalMeterQueryFilters extends QueryFilters {

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "containsFacility":
        return PHYSICAL_METER.externalId.contains(values.get(0).toLowerCase());
      case "containsSecondaryAddress":
        return PHYSICAL_METER.address.contains(values.get(0).toLowerCase());
      case "organisation":
        return PHYSICAL_METER.organisation.id.in(toUuids(values));
      default:
        return null;
    }
  }
}
