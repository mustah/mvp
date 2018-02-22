package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import static java.lang.Long.parseLong;
import static java.util.Arrays.asList;

public class LogicalMeterToPredicateMapper extends FilterToPredicateMapper {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put("medium", Q.meterDefinition.medium::eq);
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public BooleanExpression map(Map<String, List<String>> filter) {
    Predicate predicate;

    if (filter.containsKey("after")
      && filter.containsKey("before")
    // TODO add constraint, filter must contain status
    ) {
      predicate = mapPeriodicStatusFilter(filter);
    } else {
      predicate = super.map(filter);
    }

    return (BooleanExpression) predicate;
  }

  private Predicate mapPeriodicStatusFilter(Map<String, List<String>> filter) {
    // TODO Only one date is supported
    Date start = toDate(filter.get("after").get(0));
    Date stop = toDate(filter.get("before").get(0));

    // TODO get status from filter
    List<String> status = asList("Active");

    Predicate predicate = (
      // Status must have begun before period end
      Q.physicalMeters.any().statusLogs.any().start.before(stop)
        .and(
          // Status must not have ended before period start
          (Q.physicalMeters.any().statusLogs.any().stop.after(start))
            // Status may not have ended
            .or(Q.physicalMeters.any().statusLogs.any().stop.isNull())
        )
        .and(
          Q.physicalMeters.any().statusLogs.any().status.name.in(status)
        )
    ).and(
      super.map(filter)
    );

    return predicate;
  }
}
