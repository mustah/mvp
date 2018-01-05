package com.elvaco.mvp.repository.jpa.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.validation.constraints.NotNull;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class FilterToPredicateMapperTest {

  @Test
  public void mapNullFilters() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("prop", asList("1", "2", "3"));
    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        return null;
      }
    };

    assertThat(test.map(filters)).isNull();
  }

  @Test
  public void mapNullProperties() {
    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.map(null)).isNull();
  }

  @Test
  public void mapNoMatchingPropertyFilter() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("foo", singletonList("42"));
    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("bar", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.map(filters)).isNull();
  }

  @Test
  public void mapSimple() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("foo", singletonList("42"));

    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.predicate(
          Ops.EQ,
          Expressions.constant(42),
          Expressions.constant(Integer.parseInt(v))
                )
        );
        return map;
      }
    };
    BooleanExpression expected = Expressions.predicate(
      Ops.EQ,
      Expressions.constant(42),
      Expressions.constant(42)
    );

    assertThat(test.map(filters)).isEqualTo(expected);
  }

  @Test
  public void mapOredProperties() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("foo", asList("42", "43", "44"));

    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.predicate(
          Ops.EQ,
          Expressions.constant(42),
          Expressions.constant(Integer.parseInt(v))
        ));
        return map;
      }
    };
    Function<Integer, BooleanExpression> createPredicate =
      (i) -> Expressions.predicate(
        Ops.EQ,
        Expressions.constant(42),
        Expressions.constant(i)
      );

    BooleanExpression expected = createPredicate.apply(42)
      .or(createPredicate.apply(43))
      .or(createPredicate.apply(44));

    assertThat(test.map(filters)).isEqualTo(expected);
  }

  @Test
  public void mapAndedProperties() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("foo", singletonList("42"));
    filters.put("bar", singletonList("Woop!"));

    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.predicate(
          Ops.EQ,
          Expressions.constant(42),
          Expressions.constant(Integer.parseInt(v))
        ));
        map.put("bar", (String v) -> Expressions.predicate(
          Ops.EQ,
          Expressions.constant("Whazoom!"),
          Expressions.constant(v)
        ));
        return map;
      }
    };
    BooleanExpression expected =
      Expressions.predicate(
        Ops.EQ, Expressions.constant("Whazoom!"), Expressions.constant("Woop!"))
        .and(Expressions.predicate(
          Ops.EQ, Expressions.constant(42), Expressions.constant(42)
        ));

    assertThat(test.map(filters)).isEqualTo(expected);
  }
}
