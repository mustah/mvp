package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class DashboardUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;

  public static Optional<CollectionStats> sumCollectionStats(
    List<CollectionStats> collectionStats
  ) {
    return Optional.of(CollectionStats.asSumOf(collectionStats))
      .filter(sumStats -> sumStats.expected != 0.0);
  }

  public Optional<CollectionStats> findCollectionStats(RequestParameters parameters) {
    if (!parameters.hasParam(AFTER) || !parameters.hasParam(BEFORE)) {
      return Optional.empty();
    }

    List<CollectionStats> meterStats = logicalMeters
      .findMissingMeterReadingsCounts(parameters.ensureOrganisationFilters(currentUser)).stream()
      .map(entry -> new CollectionStats(entry.missingReadingCount, entry.readInterval))
      .collect(toList());

    return sumCollectionStats(meterStats);
  }
}
