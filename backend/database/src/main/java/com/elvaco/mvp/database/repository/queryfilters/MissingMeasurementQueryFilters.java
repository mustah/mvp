package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity.missingMeasurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;

public class MissingMeasurementQueryFilters extends QueryFilters {

  private static final QMissingMeasurementEntity MISSING_MEASUREMENT = missingMeasurementEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QGatewayEntity GATEWAY = gatewayEntity;

  private ZonedDateTime before;
  private ZonedDateTime after;

  @Override
  public Optional<Predicate> buildPredicateFor(String parameterName, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameterName, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "before":
        before = getZonedDateTimeFrom(values);
        return MISSING_MEASUREMENT.id.expectedTime.lt(before);
      case "after":
        after = getZonedDateTimeFrom(values);
        return MISSING_MEASUREMENT.id.expectedTime.goe(after);
      default:
        return null;
    }
  }
}
