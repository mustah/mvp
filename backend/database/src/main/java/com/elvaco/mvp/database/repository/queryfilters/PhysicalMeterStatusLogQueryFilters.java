package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

  private static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG =
    physicalMeterStatusLogEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    logicalMeterEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(String parameterName, List<String> values) {
    return Optional.ofNullable(nullablePredicate(parameterName, values));
  }

  @Nullable
  private Predicate nullablePredicate(String parameterName, List<String> values) {
    switch (parameterName) {
      case "physicalMeterId":
        return METER_STATUS_LOG.physicalMeterId.in(toUuids(values));
      case "id":
        return LOGICAL_METER.id.in(toUuids(values));
      case "before":
        stop = getZonedDateTimeFrom(values);
        return periodQueryFilter(start, stop);
      case "after":
        start = getZonedDateTimeFrom(values);
        return periodQueryFilter(start, stop);
      default:
        return null;
    }
  }

  @Nullable
  private static Predicate periodQueryFilter(ZonedDateTime start, ZonedDateTime stop) {
    if (start == null || stop == null) {
      return null;
    }
    return METER_STATUS_LOG.start.before(stop)
      .and(METER_STATUS_LOG.stop.isNull().or(METER_STATUS_LOG.stop.after(start)));
  }
}
