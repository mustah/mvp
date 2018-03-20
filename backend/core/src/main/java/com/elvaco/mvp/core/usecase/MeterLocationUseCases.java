package com.elvaco.mvp.core.usecase;

import java.util.function.BinaryOperator;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;

public class MeterLocationUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;

  public MeterLocationUseCases(LogicalMeters logicalMeters, AuthenticatedUser currentUser) {
    this.logicalMeters = logicalMeters;
    this.currentUser = currentUser;
  }

  public MeterSummary findAllForSummaryInfo(RequestParameters parameters) {
    return logicalMeters.findAllForSummaryInfo(setCurrentUsersOrganisationId(
      currentUser,
      parameters
    ))
      .stream()
      .reduce(new MeterSummary(), this::accumulate, combined());
  }

  private MeterSummary accumulate(MeterSummary meterSummary, LogicalMeter logicalMeter) {
    return meterSummary.add(logicalMeter);
  }

  private BinaryOperator<MeterSummary> combined() {
    return (summary, combined) -> combined;
  }
}
