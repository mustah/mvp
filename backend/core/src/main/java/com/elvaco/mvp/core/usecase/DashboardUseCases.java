package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class DashboardUseCases {

  private final LogicalMeters logicalMeters;

  public Optional<CollectionStats> findCollectionStats(RequestParameters parameters) {
    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return Optional.empty();
    }

    List<CollectionStats> meterStats = logicalMeters.findMissingMeasurements(parameters).stream()
      .map(entry -> new CollectionStats(entry.missingReadingCount, entry.expectedReadingCount))
      .collect(toList());

    return sumCollectionStats(meterStats);
  }

  public static Optional<CollectionStats> sumCollectionStats(
    List<CollectionStats> collectionStats
  ) {
    return Optional.of(CollectionStats.asSumOf(collectionStats))
      .filter(sumStats -> sumStats.expected != 0.0);
  }
}
