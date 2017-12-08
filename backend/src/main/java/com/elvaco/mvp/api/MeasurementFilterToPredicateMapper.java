package com.elvaco.mvp.api;

import com.elvaco.mvp.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class MeasurementFilterToPredicateMapper
    extends FilterToPredicateMapper {
  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;

  private static final Map<String, Function<String, BooleanExpression>>
      FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("meterId",
        (String meterId) -> Q.physicalMeter.id.eq(Long.parseLong(meterId)));

    FILTERABLE_PROPERTIES.put("id",
        (String id) -> Q.id.eq(Long.parseLong(id)));

    FILTERABLE_PROPERTIES.put("before",
        (String before) -> Q.created.before(Date.from(Instant.parse(before))));

    FILTERABLE_PROPERTIES.put("after",
        (String after) -> Q.created.after(Date.from(Instant.parse(after))));

    FILTERABLE_PROPERTIES.put("quantity",
        (String quantity) -> Q.quantity.eq(quantity));

  }

  Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }
}
