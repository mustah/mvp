package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
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

  public Optional<CollectionStats> getMeasurementsStatistics(RequestParameters parameters) {
    List<CollectionStats> meterStats = logicalMeters.findAll(
      setCurrentUsersOrganisationId(currentUser, parameters)
    )
      .stream()
      .map(logicalMeter -> LogicalMeterUseCases.getCollectionPercent(
        logicalMeter.physicalMeters,
        parameters,
        logicalMeter.meterDefinition.quantities.size()
           )
      )
      .filter(collectionStats -> collectionStats.isPresent())
      .map(collectionStats -> collectionStats.get())
      .collect(toList());

    double totalExpected = 0.0;
    double totalActual = 0.0;

    for (int x = 0; x < meterStats.size(); x++) {
      totalActual = totalActual + meterStats.get(x).actual;
      totalExpected = totalExpected + meterStats.get(x).expected;
    }

    if (totalExpected > 0.0) {
      return Optional.of(new CollectionStats(totalActual, totalExpected));
    } else {
      return Optional.empty();
    }
  }
}
