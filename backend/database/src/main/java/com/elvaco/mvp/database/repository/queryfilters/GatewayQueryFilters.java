package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.querydsl.core.types.Predicate;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity Q = QGatewayEntity.gatewayEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    if (filter.equals("organisation")) {
      return Optional.of(Q.organisationId.in(mapValues(UUID::fromString, values)));
    }
    return Optional.empty();
  }
}
