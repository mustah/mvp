package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public class MeasurementUseCases {
  private final Measurements measurements;

  public MeasurementUseCases(Measurements measurements) {
    this.measurements = measurements;
  }

  public Page<Measurement> findAllScaled(String scale, Map<String, List<String>> filterParams,
                                         Pageable pageable) {
    return measurements.findAllScaled(scale, filterParams, pageable);
  }

  public Page<Measurement> findAll(Map<String, List<String>> filterParams,
                                   Pageable pageable) {
    return measurements.findAll(filterParams, pageable);
  }

  public Measurement findById(Long id) {
    return measurements.findOne(id);
  }
}
