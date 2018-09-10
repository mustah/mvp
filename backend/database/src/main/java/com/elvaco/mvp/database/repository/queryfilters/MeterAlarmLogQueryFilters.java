package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class MeterAlarmLogQueryFilters extends QueryFilters {

  private static final QMeterAlarmLogEntity ALARM_LOG =
    QMeterAlarmLogEntity.meterAlarmLogEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(RequestParameter parameter, List<String> values) {
    return Optional.ofNullable(nullablePredicate(parameter, values));
  }

  @Nullable
  private Predicate nullablePredicate(RequestParameter parameter, List<String> values) {
    switch (parameter) {
      case PHYSICAL_METER_ID:
        return ALARM_LOG.physicalMeterId.in(toUuids(values));
      case BEFORE:
        stop = getZonedDateTimeFrom(values);
        return periodQueryFilter(start, stop);
      case AFTER:
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
    return ALARM_LOG.start.before(stop)
      .and(ALARM_LOG.stop.isNull().or(ALARM_LOG.stop.after(start)));
  }
}
