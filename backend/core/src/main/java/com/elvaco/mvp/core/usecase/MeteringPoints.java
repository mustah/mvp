package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface MeteringPoints {
  MeteringPoint findOne(Long id);

  List<MeteringPoint> findAll();

  Page<MeteringPoint> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  void save(MeteringPoint meteringPoint);

  void deleteAll();
}
