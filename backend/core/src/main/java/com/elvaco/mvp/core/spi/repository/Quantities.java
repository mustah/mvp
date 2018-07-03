package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Quantity;

public interface Quantities {

  List<Quantity> findAll();

  Optional<Quantity> findByName(String quantity);

  Quantity save(Quantity quantity);
}
