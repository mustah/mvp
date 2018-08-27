package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;

public class LocationQueryFilters extends QueryFilters {

  private static final QLocationEntity LOCATION = QLocationEntity.locationEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(RequestParameter parameter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(RequestParameter parameter, List<String> values) {
    switch (parameter) {
      case CITY:
      case Q_CITY:
        return LOCATION.city.contains(values.get(0).toLowerCase());
      case ADDRESS:
      case Q_ADDRESS:
        return LOCATION.streetAddress.contains(values.get(0).toLowerCase());
      case ORGANISATION:
        return LOGICAL_METER.organisationId.in(toUuids(values));
      default:
        return null;
    }
  }
}
