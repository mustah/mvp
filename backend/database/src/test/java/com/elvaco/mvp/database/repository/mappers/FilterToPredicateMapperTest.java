package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.validation.constraints.NotNull;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class FilterToPredicateMapperTest {

  @Test
  public void mapNullFilters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("prop", "1")
      .add("prop", "2")
      .add("prop", "3");
    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        return new HashMap<>();
      }
    };

    assertThat(test.map(parameters)).isNull();
  }

  @Test
  public void mapEmptyFilter() {
    RequestParameters parameters = new RequestParametersAdapter().setAll("foo", emptyList());

    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.map(parameters)).isNull();
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
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42");

    FilterToPredicateMapper test = new FilterToPredicateMapper() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("bar", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.map(parameters)).isNull();
  }

  @Test
  public void mapSingleParameter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42");

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

    assertThat(test.map(parameters)).isEqualTo(expected);
  }

  @Test
  public void mapOredProperties() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42")
      .add("foo", "43")
      .add("foo", "44");

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

    assertThat(test.map(parameters)).isEqualTo(expected);
  }

  @Test
  public void mapAndedProperties() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("bar", "Woop!")
      .add("foo", "42");

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

    assertThat(test.map(parameters)).isEqualTo(expected);
  }
}
