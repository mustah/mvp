package com.elvaco.mvp.core.domainmodels;

import java.util.Collections;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionStatsTest {

  @Test
  public void thirdCollected() {
    assertThat(new CollectionStats(2, 3).collectionPercentage).isEqualTo(33.33333333333333);
  }

  @Test
  public void noneCollected() {
    assertThat(new CollectionStats(1, 1).collectionPercentage).isEqualTo(0);
  }

  @Test
  public void allCollected() {
    assertThat(new CollectionStats(0, 1).collectionPercentage).isEqualTo(100);
  }

  @Test
  public void halfCollected() {
    assertThat(new CollectionStats(1, 2).collectionPercentage).isEqualTo(50);
  }

  @Test
  public void emptyParam() {
    assertThat(CollectionStats.asSumOf(Collections.emptyList()))
      .isEqualTo(new CollectionStats(0.0, 0.0));
  }

  @Test
  public void sumsOne() {
    assertThat(CollectionStats.asSumOf(
      Collections.singletonList(new CollectionStats(1.0, 1.0))
    )).isEqualTo(new CollectionStats(1.0, 1.0));
  }

  @Test
  public void sumsTwo() {
    assertThat(CollectionStats.asSumOf(
      asList(
        new CollectionStats(1.0, 1.0),
        new CollectionStats(2.0, 3.0)
      ))).isEqualTo(new CollectionStats(3.0, 4.0));
  }

  @Test
  public void doesNotSumActualWhenExpectedIsZero() {
    assertThat(CollectionStats.asSumOf(
      asList(
        new CollectionStats(1.0, 1.0),
        new CollectionStats(999.0, 0.0)
      ))).isEqualTo(new CollectionStats(1.0, 1.0));
  }
}
