package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.Quantities;

public class MockQuantities extends MockRepository<Long, Quantity> implements Quantities {

  @Override
  public List<Quantity> findAll() {
    return allMocks();
  }

  @Override
  public Optional<Quantity> findByName(String quantity) {
    return filter(q -> q.name.equals(quantity))
      .findFirst();
  }

  @Override
  public Quantity save(Quantity quantity) {
    return saveMock(quantity);
  }

  @Override
  protected Quantity copyWithId(Long id, Quantity entity) {
    return new Quantity(id, entity.name, entity.getPresentationInformation());
  }

  @Override
  protected Long generateId() {
    return nextId();
  }
}
