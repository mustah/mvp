package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;

public interface Organisations {

  List<Organisation> findAll();

  Optional<Organisation> findById(UUID id);

  Organisation save(Organisation organisation);

  void deleteById(UUID id);

  Optional<Organisation> findByCode(String code);
}
