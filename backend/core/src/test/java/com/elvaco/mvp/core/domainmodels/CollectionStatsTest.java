package com.elvaco.mvp.core.domainmodels;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionStatsTest {

  @Test
  public void emptyParam() {
    assertThat(CollectionStats.asSumOf(Collections.emptyList()))
      .isEqualTo(new CollectionStats(100.0));
  }

  @Test
  public void sumsOne() {
    assertThat(CollectionStats.asSumOf(
      List.of(new CollectionStats(100.0))
    )).isEqualTo(new CollectionStats(100.0));
  }

  @Test
  public void sumsTwo() {
    assertThat(CollectionStats.asSumOf(
      List.of(
        new CollectionStats(100.0),
        new CollectionStats(66.666666666)
      ))).isEqualTo(new CollectionStats(83.333333333));
  }
}
