package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.Test;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryFiltersTest {

  @Test
  public void mapNullFilters() {
    RequestParameters parameters =
      new RequestParametersAdapter()
        .add(ID, "1")
        .add(ID, "2")
        .add(ID, "3");

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        RequestParameter parameter, RequestParameters parameters,
        List<String> values
      ) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyFilter() {
    RequestParameters parameters = new RequestParametersAdapter().setAll(ID, emptyList());

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        RequestParameter parameter, RequestParameters parameters,
        List<String> values
      ) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(parameters)).isNull();
  }

  @Test
  public void mapEmptyProperties() {
    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        RequestParameter parameter, RequestParameters parameters,
        List<String> values
      ) {
        return Optional.empty();
      }
    };

    assertThat(test.toExpression(new RequestParametersAdapter())).isNull();
  }

  @Test
  public void mapSingleParameter() {
    RequestParameters parameters =
      new RequestParametersAdapter()
        .add(ID, "42");

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        RequestParameter parameter, RequestParameters parameters,
        List<String> values
      ) {
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
    RequestParameters parameters =
      new RequestParametersAdapter()
        .add(CITY, "Woop!")
        .add(ID, "42");

    QueryFilters test = new QueryFilters() {
      @Override
      public Optional<Predicate> buildPredicateFor(
        RequestParameter parameter, RequestParameters parameters,
        List<String> values
      ) {
        if (parameter.equals(ID)) {
          return Optional.of(
            Expressions.predicate(
              Ops.EQ,
              Expressions.constant(42),
              Expressions.constant(Integer.parseInt(values.get(0)))
            )
          );
        } else if (parameter.equals(CITY)) {
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
