package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

public class PhysicalMeterStatusLogToPredicateMapper extends FilterToPredicateMapper {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("physicalMeterId", (String id) -> Q.physicalMeterId.eq(
      Long.parseLong(id))
    );
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  public BooleanExpression map(List<Long> physicalMeterIds, Map<String, List<String>> filter) {
    return (Q.physicalMeterId.in(physicalMeterIds)).and(map(filter));
  }

  @Override
  public BooleanExpression map(Map<String, List<String>> filter) {
    if (filter.containsKey("after") && filter.containsKey("before")) {
      return (BooleanExpression) mapPeriodicStatusFilter(filter);
    } else {
      return super.map(filter);
    }
  }

  private Predicate mapPeriodicStatusFilter(Map<String, List<String>> filter) {
    // TODO Only one date is supported
    Date start = toDate(filter.get("after").get(0));
    Date stop = toDate(filter.get("before").get(0));

    Predicate predicate = (
      // Status must have begun before period end
      Q.start.before(stop)
        .and(
          // Status must not have ended before period start
          (Q.stop.after(start))
            // Status may not have ended
            .or(Q.stop.isNull())
        )
    ).and(
      super.map(filter)
    );

    return predicate;
  }
}
