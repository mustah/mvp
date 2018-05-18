package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.util.LogicalMeterHelper;

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

  public Optional<CollectionStats> getMeasurementsStatistics(RequestParameters parameters) {

    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return Optional.empty();
    }

    ZonedDateTime after = ZonedDateTime.parse(parameters.getFirst("after"));
    ZonedDateTime before = ZonedDateTime.parse(parameters.getFirst("before"));
    List<CollectionStats> meterStats = logicalMeters.findAll(
      setCurrentUsersOrganisationId(currentUser, parameters)
    ).stream()
      .map(logicalMeter -> LogicalMeterHelper.getCollectionPercent(
        logicalMeter.physicalMeters,
        after,
        before,
        logicalMeter.meterDefinition.quantities.size()
        )
      )
      .collect(toList());

    return sumCollectionStats(meterStats);
  }

  static Optional<CollectionStats> sumCollectionStats(List<CollectionStats> meterStats) {
    CollectionStats sumStats = CollectionStats.asSumOf(meterStats);
    if (sumStats.expected == 0.0) {
      return Optional.empty();
    }
    return Optional.of(sumStats);
  }
}
