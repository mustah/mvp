package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;

public class MeteringPointsUseCases {

  private final MeteringPoints meteringPoints;

  public MeteringPointsUseCases(MeteringPoints meteringPoints) {
    this.meteringPoints = meteringPoints;
  }

  public List<MeteringPoint> findAll() {
    return meteringPoints.findAll();
  }
}
