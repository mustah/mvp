package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import org.jooq.Field;
import org.junit.Test;

import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;

public class SortUtilTest {

  private static final Field<Object> SOMETHING_ELSE = field("somethingElse");
  private static final Field<?> SOMETHING = field("something");

  @Test
  public void resolveSortFields_emptySort() {
    assertThat(resolveSortFields(sortParametersOf(""), emptyMap())).isEmpty();
  }

  @Test
  public void resolveSortFields_noMatchingSort() {
    assertThat(resolveSortFields(sortParametersOf("something,asc"), emptyMap())).isEmpty();

    assertThat(resolveSortFields(
      sortParametersOf("something,asc"),
      Map.of("somethingElse", SOMETHING_ELSE)
    )).isEmpty();
  }

  @Test
  public void resolveSortFields_noDirectionDefaultsToAsc() {
    assertThat(resolveSortFields(
      sortParametersOf("something"),
      Map.of("something", SOMETHING)
    )).containsExactly(SOMETHING.asc());
  }

  @Test
  public void resolveSortFields_matchingAsc() {
    assertThat(resolveSortFields(
      sortParametersOf("something,asc"),
      Map.of("something", SOMETHING)
    )).containsExactly(SOMETHING.asc());
  }

  @Test
  public void resolveSortFields_matchingDesc() {
    assertThat(resolveSortFields(
      sortParametersOf("something,desc"),
      Map.of("something", SOMETHING)
    )).containsExactly(SOMETHING.desc());
  }

  @Test
  public void resolveSortFields_invalidDirection() {
    assertThat(resolveSortFields(
      sortParametersOf("something,forward"),
      Map.of("something", SOMETHING)
    )).containsExactly(SOMETHING.asc());
  }

  private RequestParameters sortParametersOf(String sortString) {
    return RequestParametersAdapter.of(Map.of(
      RequestParameter.SORT.toString(),
      List.of(sortString)
    ));
  }
}

