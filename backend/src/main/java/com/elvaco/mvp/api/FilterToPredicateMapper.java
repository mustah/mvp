package com.elvaco.mvp.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * A mapper of property filters to QueryDsl predicates.
 */
abstract class FilterToPredicateMapper {

  /**
   * Returns a mapping of filterable property names to property filter functions.
   *
   * @return A mapping of property names to property filter functions
   */
  abstract Map<String, Function<String, BooleanExpression>> getPropertyFilters();

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
   * @param filter The filter to map
   *
   * @return A QueryDsl Predicate representation of that filter, or null if no predicate could be
   *   constructed.
   */
  BooleanExpression map(Map<String, List<String>> filter) {
    if (filter == null) {
      return null;
    }

    List<BooleanExpression> predicates = new ArrayList<>();
    for (Map.Entry<String, List<String>> propertyFilter : filter.entrySet()) {
      List<String> propertyValues = propertyFilter.getValue();

      Map<String, Function<String, BooleanExpression>> filterableProperties = getPropertyFilters();
      if (filterableProperties == null) {
        filterableProperties = new HashMap<>();
      }

      Function<String, BooleanExpression> lambda =
        filterableProperties.get(propertyFilter.getKey());
      if (lambda == null) {
        continue;
      }
      String param = propertyValues.get(0);
      BooleanExpression p = lambda.apply(param);
      // Multiple filters for the same property are OR'ed together
      for (String val : propertyValues.subList(1, propertyValues.size())) {
        p = p.or(lambda.apply(val));
      }
      predicates.add(p);
    }
    if (predicates.isEmpty()) {
      return null;
    }

    BooleanExpression predicate = predicates.remove(0);
    // Filters for different properties are AND'ed
    for (BooleanExpression p : predicates) {
      predicate = predicate.and(p);
    }
    return predicate;
  }
}
