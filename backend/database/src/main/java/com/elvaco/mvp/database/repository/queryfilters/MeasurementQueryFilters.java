package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;
import static java.lang.Long.parseLong;

public class MeasurementQueryFilters extends QueryFilters {

  private static final QMeasurementEntity Q = QMeasurementEntity.measurementEntity;

  private static final Map<String, Function<String, Predicate>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  private static ZonedDateTime toZonedDateTime(String when) {
    return ZonedDateTime.parse(when);
  }

  @Override
  public Map<String, Function<String, Predicate>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public Predicate toExpression(RequestParameters parameters) {
    return propertiesExpression(parameters);
  }

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put(BEFORE, (String before) -> Q.created.before(toZonedDateTime(before)));

    FILTERABLE_PROPERTIES.put(AFTER, (String after) -> Q.created.after(toZonedDateTime(after)));

    FILTERABLE_PROPERTIES.put("quantity", Q.quantity::eq);

    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String id) -> Q.physicalMeter.organisation.id.eq(UUID.fromString(id))
    );

    FILTERABLE_PROPERTIES.put(
      "meterId",
      (String meterId) -> Q.physicalMeter.id.eq(UUID.fromString(meterId))
    );
  }
}