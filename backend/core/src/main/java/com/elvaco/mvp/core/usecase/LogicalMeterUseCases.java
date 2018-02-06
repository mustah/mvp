package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationFilter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public class LogicalMeterUseCases {

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;

  public LogicalMeterUseCases(AuthenticatedUser currentUser, LogicalMeters logicalMeters) {
    this.currentUser = currentUser;
    this.logicalMeters = logicalMeters;
  }

  public LogicalMeter findById(Long id) {
    return logicalMeters.findById(id);
  }

  public List<LogicalMeter> findAll() {
    return logicalMeters.findAll();
  }

  public Page<LogicalMeter> findAll(
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return logicalMeters.findAll(
      OrganisationFilter.complementFilterWithOrganisationParameters(currentUser, filterParams),
      pageable
    );
  }
}
