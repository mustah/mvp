package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;
import static java.util.stream.Collectors.toList;

public class DashboardUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;

  public DashboardUseCases(
    AuthenticatedUser currentUser,
    LogicalMeters logicalMeters
  ) {
    this.currentUser = currentUser;
    this.logicalMeters = logicalMeters;
  }

  static Optional<CollectionStats> sumCollectionStats(List<CollectionStats> meterStats) {
    return Optional.of(CollectionStats.asSumOf(meterStats))
      .filter(sumStats -> sumStats.expected != 0.0);
  }

  public Optional<CollectionStats> getMeasurementsStatistics(RequestParameters parameters) {

    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return Optional.empty();
    }

    List<CollectionStats> meterStats = logicalMeters.findAllWithStatuses(
      setCurrentUsersOrganisationId(currentUser, parameters)
    ).stream().map(LogicalMeter::getCollectionStats).collect(toList());

    return sumCollectionStats(meterStats);
  }
}
