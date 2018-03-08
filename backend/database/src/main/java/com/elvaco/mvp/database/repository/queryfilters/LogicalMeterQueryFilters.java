package com.elvaco.mvp.database.repository.queryfilters;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.dsl.BooleanExpression;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  private static final Map<String, Function<String, BooleanExpression>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put("id", (String id) -> Q.id.eq(UUID.fromString(id)));

    FILTERABLE_PROPERTIES.put("medium", Q.meterDefinition.medium::eq);

    FILTERABLE_PROPERTIES.put("manufacturer", Q.physicalMeters.any().manufacturer::eq);

    FILTERABLE_PROPERTIES.put("city.id", Q.location.city::eq);

    FILTERABLE_PROPERTIES.put("address.id", Q.location.streetAddress::eq);

    FILTERABLE_PROPERTIES.put(
      "organisation",
      (String id) -> Q.organisationId.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Override
  public BooleanExpression toExpression(RequestParameters parameters) {
    if (parameters.hasName("after") && parameters.hasName("before")) {
      return periodAndStatusQueryFilter(parameters);
    } else {
      return super.toExpression(parameters);
    }
  }

  private BooleanExpression periodAndStatusQueryFilter(RequestParameters parameters) {
    // TODO Only one date is supported
    Date start = toDate(parameters.getFirst("after"));
    Date stop = toDate(parameters.getFirst("before"));

    return (
      // Status must have begun before period end
      Q.physicalMeters.any().statusLogs.any().start.before(stop)
        .and(
          // Status must not have ended before period start
          Q.physicalMeters.any().statusLogs.any().stop.after(start)
            // Status may not have ended
            .or(Q.physicalMeters.any().statusLogs.any().stop.isNull())
        )
        .and(
          Q.physicalMeters.any().statusLogs.any().status.name.in(parameters.getValues("status"))
        )
    ).and(
      super.toExpression(parameters)
    );
  }

  private static Date toDate(String before) {
    return Date.from(Instant.parse(before));
  }
}
