package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  @Override
  public Optional<Predicate> prePredicateHook(RequestParameters parameters) {
    if (parameters.hasName(AFTER) && parameters.hasName(BEFORE)) {
      return Optional.of(periodQueryFilter(
        parameters.getFirst(AFTER),
        parameters.getFirst(BEFORE)
      ));
    }
    return Optional.empty();
  }

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    if (filter.equals("physicalMeterId")) {
      return Optional.of(Q.physicalMeterId.in(mapValues(UUID::fromString, values)));
    }
    return Optional.empty();
  }

  private Predicate periodQueryFilter(String after, String before) {
    ZonedDateTime start = ZonedDateTime.parse(after);
    ZonedDateTime stop = ZonedDateTime.parse(before);
    return
      Q.start.before(stop)
        .and(Q.stop.after(start).or(Q.stop.isNull()));
  }
}
