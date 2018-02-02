package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface Measurements {

  Page<Measurement> findAllByScale(
    String scale,
    Map<String, List<String>> filterParams,
    Pageable pageable
  );

  Page<Measurement> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  Optional<Measurement> findById(Long id);
}
