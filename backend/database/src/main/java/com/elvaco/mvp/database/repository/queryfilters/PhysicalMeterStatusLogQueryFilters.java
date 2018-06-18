package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

  private static final QPhysicalMeterStatusLogEntity PHYSICAL_METER =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "physicalMeterId":
        return Optional.of(PHYSICAL_METER.physicalMeterId.in(toUuids(values)));
      case "id":
        return Optional.of(LOGICAL_METER.id.in(toUuids(values)));
      case "before":
        stop = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(periodQueryFilter(start, stop));
      case "after":
        start = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(periodQueryFilter(start, stop));
      default:
        return Optional.empty();
    }
  }

  @Nullable
  private static Predicate periodQueryFilter(ZonedDateTime start, ZonedDateTime stop) {
    if (start == null || stop == null) {
      return null;
    }
    return PHYSICAL_METER.start.before(stop)
      .and(PHYSICAL_METER.stop.after(start).or(PHYSICAL_METER.stop.isNull()));
  }
}
