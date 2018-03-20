package com.elvaco.mvp.database.repository.queryfilters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;

import com.querydsl.core.types.Predicate;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity Q = QGatewayEntity.gatewayEntity;

  private static final Map<String, Function<String, Predicate>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String id) -> Q.organisationId.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, Predicate>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public Predicate toExpression(RequestParameters parameters) {
    return propertiesExpression(parameters);
  }
}
