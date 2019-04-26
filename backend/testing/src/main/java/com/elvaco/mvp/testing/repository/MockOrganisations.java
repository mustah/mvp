package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static java.util.UUID.randomUUID;

public class MockOrganisations extends MockRepository<UUID, Organisation> implements Organisations {

  public MockOrganisations() {}

  public MockOrganisations(List<Organisation> organisations) {
    organisations.forEach(this::saveMock);
  }

  @Override
  public List<Organisation> findAll() {
    return allMocks();
  }

  @Override
  public List<Organisation> findOrganisationAndSubOrganisations(UUID organisationId) {
    throw new NotImplementedYet();
  }

  @Override
  public List<Organisation> findAllSubOrganisations(UUID organisationId) {
    throw new NotImplementedYet();
  }

  @Override
  public Page<Organisation> findAllMainOrganisations(
    RequestParameters parameters, Pageable pageable
  ) {
    throw new NotImplementedYet();
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
  public void deleteById(UUID id) {}

  @Override
  public Optional<Organisation> findBySlug(String slug) {
    return filter(organisation -> organisation.slug.equals(slug))
      .findFirst();
  }

  @Override
  public Optional<Organisation> findByExternalId(String externalId) {
    return filter(organisation -> organisation.externalId.equals(externalId))
      .findFirst();
  }

  @Override
  protected Organisation copyWithId(UUID id, Organisation organisation) {
    return organisation.toBuilder().id(id).build();
  }

  @Override
  protected UUID generateId(Organisation entity) {
    return randomUUID();
  }
}
