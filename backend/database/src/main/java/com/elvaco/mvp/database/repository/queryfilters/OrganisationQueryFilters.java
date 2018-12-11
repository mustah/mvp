package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import com.querydsl.core.types.Predicate;

public class OrganisationQueryFilters extends QueryFilters {

  @Override
  public Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
    return Optional.ofNullable(buildNullablePredicateFor(parameter, parameters, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  ) {
    switch (parameter) {
      case ORGANISATION:
      case Q_ORGANISATION:
      case WILDCARD:
        String str = values.get(0);
        return ORGANISATION.name.startsWithIgnoreCase(str);
      default:
        return null;
    }
  }
}
