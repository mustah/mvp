package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.querydsl.core.types.Predicate;

public class GatewayStatusLogQueryFilters extends QueryFilters {

  private static final QGatewayStatusLogEntity STATUS_LOG =
    QGatewayStatusLogEntity.gatewayStatusLogEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "gatewayId":
        return Optional.of(STATUS_LOG.gatewayId.in(mapValues(UUID::fromString, values)));
      case "before":
        stop = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(periodQueryFilter(start, stop));
      case "after":
        start = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(periodQueryFilter(start, stop));
      default:
        return Optional.empty();
    }
  }

  @Nullable
  private static Predicate periodQueryFilter(ZonedDateTime start, ZonedDateTime stop) {
    if (start == null || stop == null) {
      return null;
    }
    return STATUS_LOG.start.before(stop)
      .and(STATUS_LOG.stop.after(start).or(STATUS_LOG.stop.isNull()));
  }
}
