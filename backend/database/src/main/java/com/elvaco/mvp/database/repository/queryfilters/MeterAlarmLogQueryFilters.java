package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class MeterAlarmLogQueryFilters extends QueryFilters {

  private static final Predicate[] NO_PREDICATE = new Predicate[0];

  private ZonedDateTime start;
  private ZonedDateTime stop;

  public static Predicate[] isWithinPeriod(RequestParameters parameters) {
    return parameters.getPeriod()
      .map(selectionPeriod -> new Predicate[] {
        isAlarmLogIsWithinInterval(selectionPeriod.start, selectionPeriod.stop)
      })
      .orElse(NO_PREDICATE);
  }

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
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
    return isAlarmLogIsWithinInterval(start, stop);
  }

  private static Predicate isAlarmLogIsWithinInterval(ZonedDateTime start, ZonedDateTime stop) {
    return ALARM_LOG.start.before(stop)
      .and(ALARM_LOG.stop.isNull().or(ALARM_LOG.stop.after(start)));
  }
}
