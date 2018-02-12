package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

public class MeterStatusLogToPredicateMapper extends FilterToPredicateMapper {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    //FILTERABLE_PROPERTIES.put(
    //  "meterId", (String id) -> Q.physicalMeter.logicalMeterId.eq(parseLong(id))
    //);
    //
    //FILTERABLE_PROPERTIES.put(
    //  "organisation",
    //  (String orgId) -> Q.physicalMeter.organisation.id.eq(parseLong(orgId))
    //);

    FILTERABLE_PROPERTIES.put("status", Q.status.name::eq);

    //TODO add test scenarios
    //TODO will this logic be understandable to the end user?
    FILTERABLE_PROPERTIES.put("before", (String before) -> (
      Q.stop.before(toDate(before)))
      .or(Q.start.before(toDate(before)))
    );

    FILTERABLE_PROPERTIES.put("after", (String after) -> (
      Q.stop.after(toDate(after)))
      .or(Q.start.after(toDate(after)))
    );

  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }
}
