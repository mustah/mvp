package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;
import static java.util.stream.Collectors.toList;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final Map<String, Function<String, Predicate>>
    FILTERABLE_PROPERTIES = new HashMap<>();

  static {
    FILTERABLE_PROPERTIES.put(
      "physicalMeterId",
      (String id) -> Q.physicalMeterId.eq(UUID.fromString(id))
    );
  }

  @Override
  public Map<String, Function<String, Predicate>> getPropertyFilters() {
    return FILTERABLE_PROPERTIES;
  }

  @Nullable
  @Override
  public Predicate toExpression(@Nullable RequestParameters parameters) {
    if (parameters != null) {
      return Q.physicalMeterId
        .in(getPhysicalMeterIds(parameters))
        .and(applyPeriodQueryFilter(parameters));
    }
    return null;
  }

  private List<UUID> getPhysicalMeterIds(RequestParameters parameters) {
    return parameters.getValues("physicalMeterIds")
      .stream()
      .map(UUID::fromString)
      .collect(toList());
  }

  private Predicate applyPeriodQueryFilter(RequestParameters parameters) {
    if (parameters.hasName(AFTER) && parameters.hasName(BEFORE)) {
      return periodQueryFilter(parameters);
    } else {
      return propertiesExpression(parameters);
    }
  }

  private Predicate periodQueryFilter(RequestParameters parameters) {
    ZonedDateTime start = ZonedDateTime.parse(parameters.getFirst(AFTER));
    ZonedDateTime stop = ZonedDateTime.parse(parameters.getFirst(BEFORE));
    return
      Q.start.before(stop)
        .and(Q.stop.after(start).or(Q.stop.isNull()))
        .and(propertiesExpression(parameters));
  }
}
