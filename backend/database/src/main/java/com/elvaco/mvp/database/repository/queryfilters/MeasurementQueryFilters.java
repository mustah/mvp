package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(
      buildNullablePredicateFor(filter, values)
    );
  }

  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "meterId":
        return Q.physicalMeter.id.in(mapValues(UUID::fromString, values));
      case "organisation":
        return Q.physicalMeter.organisation.id.in(mapValues(UUID::fromString, values));
      case "quantity":
        return Q.quantity.in(values);
      case "id":
        return Q.id.in(mapValues(Long::parseLong, values));
      case BEFORE:
        return applyOrPredicates(
          (String before) -> Q.created.before(ZonedDateTime.parse(before)), values
        );
      case AFTER:
        return applyOrPredicates(
          (String after) -> Q.created.after(ZonedDateTime.parse(after)), values
        );
      default:
        return null;
    }
  }
}
