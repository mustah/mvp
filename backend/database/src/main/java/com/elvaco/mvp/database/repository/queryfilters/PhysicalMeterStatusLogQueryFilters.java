package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;

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
        return Optional.of(PHYSICAL_METER.physicalMeterId.in(mapValues(UUID::fromString, values)));
      case "id":
        return Optional.of(LOGICAL_METER.id.in(mapValues(UUID::fromString, values)));
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
  private static Predicate periodQueryFilter(ZonedDateTime periodStart, ZonedDateTime periodStop) {
    if (periodStart == null || periodStop == null) {
      return null;
    }
    return PHYSICAL_METER.start.before(periodStop)
      .and(PHYSICAL_METER.stop.after(periodStart).or(PHYSICAL_METER.stop.isNull()));
  }
}
