package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.OptionalDouble;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;

@RequiredArgsConstructor
public class DashboardUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;

  public Optional<CollectionStats> findCollectionStats(RequestParameters parameters) {
    if (!parameters.hasParam(AFTER) || !parameters.hasParam(BEFORE)) {
      return Optional.empty();
    }

    OptionalDouble percent =
      logicalMeters.findAllCollectionStatsPerDate(parameters.ensureOrganisationFilters(currentUser))
        .stream()
        .mapToDouble(CollectionStatsPerDateDto::getCollectionPercentage).average();

    return percent.isPresent()
      ? Optional.of(new CollectionStats(percent.getAsDouble()))
      : Optional.empty();
  }
}
