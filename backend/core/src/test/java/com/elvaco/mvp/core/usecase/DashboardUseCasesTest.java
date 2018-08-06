package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import org.junit.Test;

import static com.elvaco.mvp.core.usecase.DashboardUseCases.sumCollectionStats;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DashboardUseCasesTest {

  @Test
  public void emptyCollectionsReturnsEmpty() {
    assertThat(sumCollectionStats(emptyList())).isEmpty();
  }

  @Test
  public void zeroExpectedReturnsEmpty() {
    assertThat(sumCollectionStats(singletonList(new CollectionStats(0, 0)))).isEmpty();
  }

  @Test
  public void isPresentWhenExpectedGreaterThanZero() {
    assertThat(sumCollectionStats(singletonList(new CollectionStats(0, 1)))).isPresent();
    assertThat(sumCollectionStats(singletonList(new CollectionStats(4, 7)))).isPresent();
  }
}
