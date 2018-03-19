package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;

import static java.util.UUID.randomUUID;

public class MockOrganisations extends MockRepository<UUID, Organisation> implements Organisations {

  public MockOrganisations() {}

  public MockOrganisations(List<Organisation> organisations) {
    organisations.forEach(this::saveMock);
  }

  @Override
  protected Organisation copyWithId(UUID id, Organisation entity) {
    return new Organisation(id, entity.name, entity.slug);
  }

  @Override
  protected UUID generateId() {
    return randomUUID();
  }

  @Override
  public List<Organisation> findAll() {
    return allMocks();
  }

  @Override
  public Optional<Organisation> findById(UUID id) {
    return filter(o -> o.id.equals(id))
      .findFirst();
  }

  @Override
  public Organisation save(Organisation organisation) {
    return saveMock(organisation);
  }

  @Override
  public void deleteById(UUID id) {

  }

  @Override
  public Optional<Organisation> findBySlug(String slug) {
    return filter(organisation -> organisation.slug.equals(slug))
      .findFirst();
  }
}
