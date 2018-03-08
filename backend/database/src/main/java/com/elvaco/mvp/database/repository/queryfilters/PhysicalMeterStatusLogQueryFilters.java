package com.elvaco.mvp.database.repository.queryfilters;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

import static java.util.stream.Collectors.toList;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

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
  public BooleanExpression toExpression(RequestParameters parameters) {
    return Q.physicalMeterId
      .in(getPhysicalMeterIds(parameters))
      .and(applyPeriodQueryFilter(parameters));
  }

  private List<UUID> getPhysicalMeterIds(RequestParameters parameters) {
    return parameters.getValues("physicalMeterIds")
      .stream()
      .map(UUID::fromString)
      .collect(toList());
  }

  private BooleanExpression applyPeriodQueryFilter(RequestParameters parameters) {
    if (parameters.hasName("after") && parameters.hasName("before")) {
      return periodQueryFilter(parameters);
    } else {
      return super.toExpression(parameters);
    }
  }

  private BooleanExpression periodQueryFilter(RequestParameters parameters) {
    // TODO Only one date is supported
    Date start = toDate(parameters.getFirst("after"));
    Date stop = toDate(parameters.getFirst("before"));
    return
      Q.start.before(stop)
        .and(
          Q.stop.after(start).or(Q.stop.isNull())
        )
        .and(
          super.toExpression(parameters)
        );
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }
}
