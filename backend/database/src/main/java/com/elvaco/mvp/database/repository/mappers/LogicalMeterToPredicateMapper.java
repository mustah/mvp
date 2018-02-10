package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;

import com.querydsl.core.types.dsl.BooleanExpression;

import static java.lang.Long.parseLong;

public class LogicalMeterToPredicateMapper extends FilterToPredicateMapper {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put("medium", Q.meterDefinition.medium::eq);

    FILTERABLE_PROPERTIES.put("status", Q.status::eq);

    FILTERABLE_PROPERTIES.put("before", (String before) -> Q.created.before(toDate(before)));

    FILTERABLE_PROPERTIES.put("after", (String after) -> Q.created.after(toDate(after)));
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }
}
