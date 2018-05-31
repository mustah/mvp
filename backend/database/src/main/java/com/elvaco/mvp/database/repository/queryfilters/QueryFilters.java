package com.elvaco.mvp.database.repository.queryfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import com.elvaco.mvp.core.exception.PredicateConstructionFailure;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * A mapper of property filters to QueryDsl predicates.
 */
public abstract class QueryFilters {

  public abstract Optional<Predicate> buildPredicateFor(String filter, List<String> values);

  public final Predicate toExpression(RequestParameters parameters) {
    if (parameters.isEmpty()) {
      return null;
    }

    List<Predicate> predicates = new ArrayList<>();
    for (Entry<String, List<String>> propertyFilter : parameters.entrySet()) {
      List<String> values = propertyFilter.getValue();
      if (!values.isEmpty()) {
        String name = propertyFilter.getKey();
        try {
          buildPredicateFor(name, values).ifPresent(predicates::add);
        } catch (Exception exception) {
          throw new PredicateConstructionFailure(name, values, exception);
        }
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    return applyAndPredicates(predicates);
  }

  final <T> List<T> mapValues(Function<String, T> function, List<String> values) {
    return values.stream().map(function).collect(toList());
  }

  private Predicate applyAndPredicates(List<Predicate> predicates) {
    return ExpressionUtils.allOf(predicates);
  }
}
