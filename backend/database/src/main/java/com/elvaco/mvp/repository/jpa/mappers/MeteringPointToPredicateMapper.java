package com.elvaco.mvp.repository.jpa.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.entity.meteringpoint.QMeteringPointEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

import static java.lang.Long.parseLong;

public class MeteringPointToPredicateMapper extends FilterToPredicateMapper {

  private static final QMeteringPointEntity Q = QMeteringPointEntity.meteringPointEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(parseLong(id)));

    FILTERABLE_PROPERTIES.put("medium", Q.medium::eq);

    FILTERABLE_PROPERTIES.put("status", Q.status::eq);

  }
}
