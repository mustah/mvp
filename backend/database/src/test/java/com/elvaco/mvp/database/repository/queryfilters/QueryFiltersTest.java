package com.elvaco.mvp.database.repository.queryfilters;

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

public class QueryFiltersTest {

  @Test
  public void mapNullFilters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("prop", "1")
      .add("prop", "2")
      .add("prop", "3");

    QueryFilters test = new AbstractQueryFilters() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        return new HashMap<>();
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyFilter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll("foo", emptyList());

    QueryFilters test = new AbstractQueryFilters() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyProperties() {
    QueryFilters test = new AbstractQueryFilters() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.toExpression(new RequestParametersAdapter())).isNull();
  }

  @Test
  public void mapNoMatchingPropertyFilter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42");

    QueryFilters test = new AbstractQueryFilters() {
      @Override
      @NotNull
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("bar", (String v) -> Expressions.FALSE);
        return map;
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapSingleParameter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42");

    QueryFilters test = new AbstractQueryFilters() {
      @Override
      @NotNull
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

    BooleanExpression expected = Expressions.predicate(
      Ops.EQ,
      Expressions.constant(42),
      Expressions.constant(42)
    );

    assertThat(test.toExpression(parameters)).isEqualTo(expected);
  }

  @Test
  public void mapOredProperties() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "55")
      .add("foo", "56")
      .add("foo", "57");

    QueryFilters test = new AbstractQueryFilters() {
      @Override
      public Map<String, Function<String, BooleanExpression>> getPropertyFilters() {
        Map<String, Function<String, BooleanExpression>> map = new HashMap<>();
        map.put("foo", (String v) -> Expressions.predicate(
          Ops.EQ,
          Expressions.constant(55),
          Expressions.constant(Integer.parseInt(v))
        ));
        return map;
      }
    };
    Function<Integer, BooleanExpression> createPredicate =
      (i) -> Expressions.predicate(
        Ops.EQ,
        Expressions.constant(55),
        Expressions.constant(i)
      );

    BooleanExpression expected = createPredicate.apply(55)
      .or(createPredicate.apply(56))
      .or(createPredicate.apply(57));

    assertThat(test.toExpression(parameters)).isEqualTo(expected);
  }

  @Test
  public void mapAndedProperties() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("bar", "Woop!")
      .add("foo", "42");

    QueryFilters test = new AbstractQueryFilters() {
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
          Ops.EQ,
          Expressions.constant(42),
          Expressions.constant(42)
        ));

    assertThat(test.toExpression(parameters)).isEqualTo(expected);
  }

  private static abstract class AbstractQueryFilters extends QueryFilters {
    @Override
    public BooleanExpression toExpression(RequestParameters parameters) {
      return propertiesExpression(parameters);
    }
  }
}
