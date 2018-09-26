package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;

public class MissingMeasurementQueryFilters extends QueryFilters {

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
      case BEFORE:
        return MISSING_MEASUREMENT.id.expectedTime.lt(getZonedDateTimeFrom(values));
      case AFTER:
        return MISSING_MEASUREMENT.id.expectedTime.goe(getZonedDateTimeFrom(values));
      default:
        return null;
    }
  }
}
