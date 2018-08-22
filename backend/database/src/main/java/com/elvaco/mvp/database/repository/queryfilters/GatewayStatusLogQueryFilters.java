package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class GatewayStatusLogQueryFilters extends QueryFilters {

  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG =
    gatewayStatusLogEntity;

  private static final QGatewayEntity GATEWAY =
    gatewayEntity;

  private ZonedDateTime start;
  private ZonedDateTime stop;

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    List<String> values
  ) {
    return Optional.ofNullable(nullablePredicate(parameter, values));
  }

  @Nullable
  private Predicate nullablePredicate(
    RequestParameter parameter,
    List<String> values
  ) {
    switch (parameter) {
      case GATEWAY_ID:
        return GATEWAY_STATUS_LOG.gatewayId.in(toUuids(values));
      case ID:
        return GATEWAY.id.in(toUuids(values));
      case BEFORE:
        stop = getZonedDateTimeFrom(values);
        return periodQueryFilter(start, stop);
      case AFTER:
        start = getZonedDateTimeFrom(values);
        return periodQueryFilter(start, stop);
      default:
        return null;
    }
  }

  @Nullable
  private static Predicate periodQueryFilter(ZonedDateTime start, ZonedDateTime stop) {
    if (start == null || stop == null) {
      return null;
    }
    return GATEWAY_STATUS_LOG.start.before(stop)
      .and(GATEWAY_STATUS_LOG.stop.isNull().or(GATEWAY_STATUS_LOG.stop.after(start)));
  }
}
