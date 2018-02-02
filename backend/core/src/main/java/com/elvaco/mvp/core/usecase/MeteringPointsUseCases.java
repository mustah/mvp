package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationFilter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public class MeteringPointsUseCases {

  private final MeteringPoints meteringPoints;
  private final AuthenticatedUser currentUser;

  public MeteringPointsUseCases(AuthenticatedUser currentUser, MeteringPoints meteringPoints) {
    this.currentUser = currentUser;
    this.meteringPoints = meteringPoints;
  }

  public MeteringPoint findOne(Long id) {
    return meteringPoints.findOne(id);
  }

  public List<MeteringPoint> findAll() {
    return meteringPoints.findAll();
  }

  public Page<MeteringPoint> findAll(
    Map<String, List<String>> filterParams,
    Pageable pageable
  ) {
    return meteringPoints.findAll(
      OrganisationFilter.complementFilterWithOrganisationParameters(currentUser, filterParams),
      pageable
    );
  }
}
