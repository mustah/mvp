package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
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
    List<PhysicalMeter> meterList = logicalMeters.findAll(
      setCurrentUsersOrganisationId(currentUser, parameters)
    )
      .stream()
      .map(logicalMeter -> logicalMeter.physicalMeters)
      .flatMap(physicalMeters -> physicalMeters.stream())
      .collect(toList());

    return LogicalMeterUseCases.getCollectionPercent(meterList, parameters);
  }
}
