package com.elvaco.mvp.core.domainmodels;

import java.util.Collection;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class CollectionStats {

  public final double collectionPercentage;

  public static CollectionStats asSumOf(Collection<CollectionStats> stats) {
    if (stats.isEmpty()) {
      return new CollectionStats(100.0);
    }

    double collectionPercentage = 0.0;
    for (CollectionStats meterStat : stats) {
      collectionPercentage += meterStat.collectionPercentage;
    }
    return new CollectionStats(collectionPercentage / stats.size());
  }
}
