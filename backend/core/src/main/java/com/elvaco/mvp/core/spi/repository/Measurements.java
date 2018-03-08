package com.elvaco.mvp.core.spi.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Measurements {

  List<Measurement> findAllByScale(String scale, RequestParameters parameters);

  List<Measurement> findAll(RequestParameters parameters);

  Optional<Measurement> findById(Long id);

  Measurement save(Measurement measurement);

  Collection<Measurement> save(Collection<Measurement> measurement);
}
