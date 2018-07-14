package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity MEASUREMENT = QMeasurementEntity.measurementEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(String parameterName, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameterName, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "physicalMeterId":
        return MEASUREMENT.physicalMeter.id.in(toUuids(values));
      case "id":
        return MEASUREMENT.physicalMeter.logicalMeterId.in(toUuids(values));
      case "manufacturer":
        return MEASUREMENT.physicalMeter.manufacturer.in(values);
      case "organisation":
        return MEASUREMENT.physicalMeter.organisation.id.in(toUuids(values));
      case "before":
        ZonedDateTime before = ZonedDateTime.parse(values.get(0));
        return MEASUREMENT.created.lt(before);
      case "after":
        ZonedDateTime after = ZonedDateTime.parse(values.get(0));
        return MEASUREMENT.created.goe(after);
      default:
        return null;
    }
  }
}
