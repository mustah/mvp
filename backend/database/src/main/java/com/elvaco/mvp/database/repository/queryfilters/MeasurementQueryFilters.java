package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.measurement.QMeasurementEntity.measurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class MeasurementQueryFilters extends QueryFilters {

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter, RequestParameters parameters, List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(RequestParameter parameter, List<String> values) {
    switch (parameter) {
      case BEFORE:
        return measurementEntity.id.created.lt(getZonedDateTimeFrom(values));
      case AFTER:
        return measurementEntity.id.created.goe(getZonedDateTimeFrom(values));
      case LOGICAL_METER_ID:
        return logicalMeterEntity.id.in(toUuids(values));
      case ORGANISATION:
        return logicalMeterEntity.organisationId.in(toUuids(values));
      default:
        return null;
    }
  }
}
