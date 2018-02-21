package com.elvaco.mvp.consumers.rabbitmq;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;

class MockOrganisations extends MockRepository<Organisation> implements com.elvaco.mvp.core.spi
  .repository.Organisations {

  @Override
  Optional<Long> getId(Organisation entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  Organisation copyWithId(Long id, Organisation entity) {
    return new Organisation(id, entity.name, entity.code);
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
