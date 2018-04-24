package com.elvaco.mvp.core.domainmodels;

public class CollectionStats {
  public final double actual;
  public final double expected;

  public CollectionStats(double actual, double expected) {
    this.actual = actual;
    this.expected = expected;
  }

  public double getCollectionPercentage() {
    return expected == 0 ? 0 : actual / expected;
  }
}
