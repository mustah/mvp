package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.CollectionStats;

import org.junit.Test;

import static com.elvaco.mvp.core.usecase.DashboardUseCases.sumCollectionStats;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class DashboardUseCasesTest {

  @Test
  public void emptyCollectionsReturnsEmpty() {
    assertThat(sumCollectionStats(emptyList())).isEmpty();
  }

  @Test
  public void isPresent() {
    assertThat(sumCollectionStats(List.of(new CollectionStats(0.0)))).isPresent();
    assertThat(sumCollectionStats(List.of(new CollectionStats(75.0)))).isPresent();
  }
}
