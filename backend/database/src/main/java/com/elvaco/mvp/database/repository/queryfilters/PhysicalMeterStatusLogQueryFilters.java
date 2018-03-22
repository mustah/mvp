package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.AFTER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.BEFORE;

public class PhysicalMeterStatusLogQueryFilters extends QueryFilters {

  private static final QPhysicalMeterStatusLogEntity Q =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  @Nullable
  @Override
  public Predicate toExpression(@Nullable RequestParameters parameters) {
    if (parameters != null) {
      return new BooleanBuilder().and(propertiesExpression(parameters))
        .and(applyPeriodQueryFilter(parameters))
        .getValue();
    }
    return null;
  }

  @Override
  public Optional<Predicate> buildPredicateFor(
    String filter, List<String> values
  ) {
    if (filter.equals("physicalMeterId")) {
      return Optional.of(Q.physicalMeterId.in(mapValues(UUID::fromString, values)));
    }
    return Optional.empty();
  }

  @Nullable
  private Predicate applyPeriodQueryFilter(RequestParameters parameters) {
    if (parameters.hasName(AFTER) && parameters.hasName(BEFORE)) {
      return periodQueryFilter(parameters.getFirst(AFTER), parameters.getFirst(BEFORE));
    }
    return null;
  }

  private Predicate periodQueryFilter(String after, String before) {
    ZonedDateTime start = ZonedDateTime.parse(after);
    ZonedDateTime stop = ZonedDateTime.parse(before);
    return
      Q.start.before(stop)
        .and(Q.stop.after(start).or(Q.stop.isNull()));
  }
}
