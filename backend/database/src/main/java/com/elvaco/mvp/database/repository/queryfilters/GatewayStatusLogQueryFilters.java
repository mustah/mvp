package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;

public class GatewayStatusLogQueryFilters extends QueryFilters {

  private static final QGatewayStatusLogEntity Q =
    QGatewayStatusLogEntity.gatewayStatusLogEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    switch (filter) {
      case "gatewayId":
        return Optional.of(Q.gatewayId.in(mapValues(UUID::fromString, values)));
      case BEFORE:
        stop = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(
          periodQueryFilter(start, stop)
        );
      case AFTER:
        start = ZonedDateTime.parse(values.get(0));
        return Optional.ofNullable(
          periodQueryFilter(start, stop)
        );
      default:
        return Optional.empty();
    }
  }

  @Nullable
  private Predicate periodQueryFilter(ZonedDateTime start, ZonedDateTime stop) {
    if (start == null || stop == null) {
      return null;
    }
    return Q.start.before(stop).and(Q.stop.after(start).or(Q.stop.isNull()));
  }
}
