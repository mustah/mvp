package com.elvaco.mvp.database.repository.queryfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * A mapper of property filters to QueryDsl predicates.
 */
public abstract class QueryFilters {

  /**
   * Returns a mapping of filterable property names to property filter functions.
   *
   * @return A mapping of property names to property filter functions
   */
  public abstract Map<String, Function<String, Predicate>> getPropertyFilters();

  @Nullable
  public abstract Predicate toExpression(RequestParameters parameters);

  /**
   * Maps a property filter to a QueryDsl Predicate.
   *
   * <p>Each unique key in the map represents a filter to be AND'ed together with all other filters
   * to form the final composite filter expression. Each value for the respective keys represents
   * a filter to be OR'ed together for its key's subexpression.
   *
   * <p>That is, given a map {@code k => [v], l => [x,y]} the resulting filter expression could
   * be written as {@code f[k](v) && (f[l](x) || f[l](y))} where {@code f[P]} is a
   * function that maps a given value to a filter expression on the property {@code P},
   *
   * <p>Note that this method intentionally returns null in the case that no predicate could be
   * constructed, since {@link com.querydsl.core.FilteredClause} explicitly ignores null arguments.
   *
   * @param parameters The filter to map
   *
   * @return A QueryDsl Predicate representation of that filter, or null if no predicate could be
   *   constructed.
   */
  @Nullable
  final Predicate propertiesExpression(RequestParameters parameters) {
    if (parameters.isEmpty()) {
      return null;
    }

    List<Predicate> predicates = new ArrayList<>();
    for (Entry<String, List<String>> propertyFilter : parameters.entrySet()) {
      List<String> propertyValues = propertyFilter.getValue();

      Function<String, Predicate> predicateFunction =
        getPropertyFilters().get(propertyFilter.getKey());

      if (predicateFunction != null && !propertyValues.isEmpty()) {
        predicates.add(applyOrPredicates(predicateFunction, propertyValues));
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    return applyAndPredicates(predicates);
  }

  private Predicate applyOrPredicates(
    Function<String, Predicate> predicateFunction,
    List<String> propertyValues
  ) {
    // Multiple filters for the same property are OR'ed together
    BooleanExpression predicate =
      (BooleanExpression) predicateFunction.apply(propertyValues.get(0));
    for (String value : propertyValues.subList(1, propertyValues.size())) {
      predicate = predicate.or(predicateFunction.apply(value));
    }
    return predicate;
  }

  private Predicate applyAndPredicates(List<Predicate> predicates) {
    BooleanExpression predicate = (BooleanExpression) predicates.remove(0);
    for (Predicate right : predicates) {
      predicate = predicate.and(right);
    }
    return predicate;
  }
}
