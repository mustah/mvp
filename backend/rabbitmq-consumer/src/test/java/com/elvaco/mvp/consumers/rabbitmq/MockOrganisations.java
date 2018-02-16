package com.elvaco.mvp.consumers.rabbitmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;

class MockOrganisations implements com.elvaco.mvp.core.spi.repository.Organisations {
  private List<Organisation> organisations;

  MockOrganisations() {
    organisations = new ArrayList<>();
  }

  @Override
  public List<Organisation> findAll() {
    return organisations;
  }

  @Override
  public Optional<Organisation> findById(Long id) {
    return organisations.stream().filter(o -> o.id.equals(id)).findFirst();
  }

  @Override
  public Organisation save(Organisation organisation) {
    if (organisation.id != null) {
      organisations.set(Math.toIntExact(organisation.id), organisation);
    } else {
      organisation = new Organisation(
        (long) organisations.size(),
        organisation.name,
        organisation.code
      );
      organisations.add(organisation);
    }
    return organisation;
  }

  @Override
  public void deleteById(Long id) {

  }

  @Override
  public Optional<Organisation> findByCode(String code) {
    return organisations.stream()
      .filter(organisation -> organisation.code.equals(code))
      .findFirst();
  }
}
