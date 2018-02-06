package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;

public interface Measurements {

  List<Measurement> findAllByScale(
    String scale,
    Map<String, List<String>> filterParams
  );

  List<Measurement> findAll(Map<String, List<String>> filterParams);

  Optional<Measurement> findById(Long id);
}
