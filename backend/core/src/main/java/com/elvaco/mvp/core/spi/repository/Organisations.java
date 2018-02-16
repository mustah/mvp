package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;

public interface Organisations {

  List<Organisation> findAll();

  Optional<Organisation> findById(Long id);

  Organisation save(Organisation organisation);

  void deleteById(Long id);

  Optional<Organisation> findByCode(String code);
}
