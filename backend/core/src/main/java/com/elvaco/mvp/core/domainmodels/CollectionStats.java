package com.elvaco.mvp.core.domainmodels;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CollectionStats {

  public final double missing;
  public final double expected;
  public final double collectionPercentage;

  public CollectionStats(double missing, double expected) {
    this.missing = missing;
    this.expected = expected;
    this.collectionPercentage = expected == 0
      ? Double.NaN
      : ((expected - missing) / expected) * 100;
  }

  public static CollectionStats asSumOf(Collection<CollectionStats> stats) {
    double missing = 0.0;
    double expected = 0.0;
    for (CollectionStats meterStat : stats) {
      if (meterStat.expected > 0.0) {
        missing += meterStat.missing;
        expected += meterStat.expected;
      }
    }
    return new CollectionStats(missing, expected);
  }

  public static CollectionStats of(@Nullable Long missing, @Nullable Long expected) {
    return new CollectionStats(
      Optional.ofNullable(missing).orElse(0L),
      Optional.ofNullable(expected).orElse(0L)
    );
  }

  @Nullable
  public Double getCollectionPercentage() {
    return Double.valueOf(collectionPercentage).isNaN() ? null : collectionPercentage;
  }
}
