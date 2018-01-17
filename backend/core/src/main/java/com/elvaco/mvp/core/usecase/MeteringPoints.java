package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;

public interface MeteringPoints {
  List<MeteringPoint> findAll();
}
