package com.elvaco.mvp.repository.jpa.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

import static java.lang.Long.parseLong;

public class MeasurementFilterToPredicateMapper extends FilterToPredicateMapper {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  static {
    FILTERABLE_PROPERTIES.put(
      "meterId", (String meterId) -> Q.physicalMeter.id.eq(parseLong(meterId))
    );

    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put("before", (String before) -> Q.created.before(toDate(before)));

    FILTERABLE_PROPERTIES.put("after", (String after) -> Q.created.after(toDate(after)));

    FILTERABLE_PROPERTIES.put("quantity", Q.quantity::eq);

    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String orgId) -> Q.physicalMeter.organisation.id.eq(parseLong(orgId))
    );
  }
}
