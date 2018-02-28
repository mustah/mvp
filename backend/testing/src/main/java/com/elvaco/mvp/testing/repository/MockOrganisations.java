package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;

public class MockOrganisations extends MockRepository<Long, Organisation> implements Organisations {

  @Override
  protected Organisation copyWithId(Long id, Organisation entity) {
    return new Organisation(id, entity.name, entity.code);
  }

  @Override
  protected Long generateId() {
    return nextId();
  }

  @Override
  public List<Organisation> findAll() {
    return allMocks();
  }

  @Override
  public Optional<Organisation> findById(Long id) {
    return filter(o -> o.id.equals(id)).findFirst();
  }

  @Override
  public Organisation save(Organisation organisation) {
    return saveMock(organisation);
  }

  @Override
  public void deleteById(Long id) {

  }

  @Override
  public Optional<Organisation> findByCode(String code) {
    return filter(organisation -> organisation.code.equals(code))
      .findFirst();
  }
}
