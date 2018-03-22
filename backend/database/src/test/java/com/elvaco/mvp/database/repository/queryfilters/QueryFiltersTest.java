package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
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

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        String filter, List<String> values
      ) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyFilter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll("foo", emptyList());

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyProperties() {
    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(new RequestParametersAdapter())).isNull();
  }

  @Test
  public void mapSingleParameter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("foo", "42");

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
        return Optional.of(
          Expressions.predicate(
            Ops.EQ,
            Expressions.constant(42),
            Expressions.constant(Integer.parseInt(values.get(0)))
          ));
      }
    };

    Predicate expected = Expressions.predicate(
      Ops.EQ,
      Expressions.constant(42),
      Expressions.constant(42)
    );

    assertThat(test.toExpression(parameters)).isEqualTo(expected);
  }

  @Test
  public void mapAndedProperties() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("bar", "Woop!")
      .add("foo", "42");

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
        if (filter.equals("foo")) {
          return Optional.of(
            Expressions.predicate(
              Ops.EQ,
              Expressions.constant(42),
              Expressions.constant(Integer.parseInt(values.get(0)))
            )
          );
        } else if (filter.equals("bar")) {
          return Optional.of(
            Expressions.predicate(
              Ops.EQ,
              Expressions.constant("Whazoom!"),
              Expressions.constant(values.get(0))
            )
          );
        }
        return Optional.empty();
      }
    };

    Predicate expected =
      Expressions.predicate(
        Ops.EQ, Expressions.constant("Whazoom!"), Expressions.constant("Woop!"))
        .and(Expressions.predicate(
          Ops.EQ,
          Expressions.constant(42),
          Expressions.constant(42)
        ));

    assertThat(test.toExpression(parameters)).isEqualTo(expected);
  }

}
