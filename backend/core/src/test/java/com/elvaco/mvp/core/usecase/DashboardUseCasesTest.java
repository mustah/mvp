package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DashboardUseCasesTest {

  @Test
  public void emptyCollectionsReturnsEmpty() {
    assertThat(DashboardUseCases.sumCollectionStats(emptyList())).isEmpty();
  }

  @Test
  public void zeroExpectedReturnsEmpty() {
    assertThat(DashboardUseCases.sumCollectionStats(singletonList(new CollectionStats(
      0.0,
      0.0
    )))).isEmpty();
  }

  @Test
  public void isPresentWhenExpectedGreaterThanZero() {
    assertThat(DashboardUseCases.sumCollectionStats(singletonList(new CollectionStats(
      0.0,
      1.0
    )))).isPresent();
  }
}
