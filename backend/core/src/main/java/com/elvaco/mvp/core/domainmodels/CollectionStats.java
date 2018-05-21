package com.elvaco.mvp.core.domainmodels;

import java.util.Collection;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CollectionStats {

  public final double actual;
  public final double expected;

  public CollectionStats(double actual, double expected) {
    this.actual = actual;
    this.expected = expected;
  }

  public static CollectionStats asSumOf(Collection<CollectionStats> stats) {
    double totalExpected = 0.0;
    double totalActual = 0.0;
    for (CollectionStats meterStat : stats) {
      if (meterStat.expected <= 0.0) {
        continue;
      }
      totalActual += meterStat.actual;
      totalExpected += meterStat.expected;
    }
    return new CollectionStats(totalActual, totalExpected);
  }

  public double getCollectionPercentage() {
    return expected == 0 ? Double.NaN : (actual / expected) * 100;
  }
}
