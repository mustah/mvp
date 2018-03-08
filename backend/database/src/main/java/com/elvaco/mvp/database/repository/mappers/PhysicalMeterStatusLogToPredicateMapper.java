package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

public class PhysicalMeterStatusLogToPredicateMapper extends FilterToPredicateMapper {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put(
      "physicalMeterId",
      (String id) -> Q.physicalMeterId.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public BooleanExpression map(RequestParameters parameters) {
    if (parameters.hasName("after") && parameters.hasName("before")) {
      return (BooleanExpression) mapPeriodicStatusFilter(parameters);
    } else {
      return super.map(parameters);
    }
  }

  public BooleanExpression map(List<UUID> physicalMeterIds, RequestParameters parameters) {
    return Q.physicalMeterId.in(physicalMeterIds).and(map(parameters));
  }

  private Predicate mapPeriodicStatusFilter(RequestParameters parameters) {
    // TODO Only one date is supported
    Date start = toDate(parameters.getFirst("after"));
    Date stop = toDate(parameters.getFirst("before"));
    return
      Q.start.before(stop)
        .and(
          Q.stop.after(start).or(Q.stop.isNull())
        )
        .and(
          super.map(parameters)
        );
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }
}
