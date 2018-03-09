package com.elvaco.mvp.database.repository.queryfilters;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

import static java.lang.Long.parseLong;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put(
      "meterId",
      (String meterId) -> Q.physicalMeter.id.eq(UUID.fromString(meterId))
    );

    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put("before", (String before) -> Q.created.before(toDate(before)));

    FILTERABLE_PROPERTIES.put("after", (String after) -> Q.created.after(toDate(after)));

    FILTERABLE_PROPERTIES.put("quantity", Q.quantity::eq);

    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String id) -> Q.physicalMeter.organisation.id.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public BooleanExpression toExpression(RequestParameters parameters) {
    return propertiesExpression(parameters);
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }
}
