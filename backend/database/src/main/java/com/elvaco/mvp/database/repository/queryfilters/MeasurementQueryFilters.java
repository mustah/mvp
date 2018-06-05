package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.Predicate;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;
  ZonedDateTime before;
  ZonedDateTime after;

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "physicalMeterId":
        return Q.physicalMeter.id.in(mapValues(UUID::fromString, values));
      case "id":
        return Q.physicalMeter.logicalMeterId.in(mapValues(UUID::fromString, values));
      case "manufacturer":
        return Q.physicalMeter.manufacturer.in(values);
      case "organisation":
        return Q.physicalMeter.organisation.id.in(mapValues(UUID::fromString, values));
      case "before": {
        before = ZonedDateTime.parse(values.get(0));
        return Q.created.lt(before);
      }
      case "after": {
        after = ZonedDateTime.parse(values.get(0));
        return Q.created.goe(after);
      }
      default:
        return null;
    }
  }
}
