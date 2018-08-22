package com.elvaco.mvp.database.repository.queryfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.exception.PredicateConstructionFailure;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

/**
 * A mapper of property filters to QueryDsl predicates.
 */
public abstract class QueryFilters {

  public abstract Optional<Predicate> buildPredicateFor(
    RequestParameter parameterName,
    List<String> values
  );

  @Nullable
  public final Predicate toExpression(RequestParameters parameters) {
    if (parameters.isEmpty()) {
      return null;
    }

    List<Predicate> predicates = new ArrayList<>();
    for (Entry<RequestParameter, List<String>> propertyFilter : parameters.entrySet()) {
      List<String> values = propertyFilter.getValue();
      if (!values.isEmpty()) {
        RequestParameter parameter = propertyFilter.getKey();
        try {
          buildPredicateFor(parameter, values).ifPresent(predicates::add);
        } catch (Exception exception) {
          throw new PredicateConstructionFailure(parameter.toString(), values, exception);
        }
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    return applyAndPredicates(predicates);
  }

  private Predicate applyAndPredicates(List<Predicate> predicates) {
    return ExpressionUtils.allOf(predicates);
  }
}
