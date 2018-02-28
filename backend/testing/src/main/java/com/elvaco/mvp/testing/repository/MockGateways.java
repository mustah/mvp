package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;

public class MockGateways extends MockRepository<Gateway> implements Gateways {

  @Override
  protected Optional<Long> getId(Gateway entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  public List<Gateway> findAll() {
    return allMocks();
  }

  @Override
  public Gateway save(Gateway gateway) {
    return saveMock(gateway);
  }

  @Override
  protected Gateway copyWithId(Long id, Gateway entity) {
    return new Gateway(
      id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      entity.meters
    );
  }

  @Override
  public List<Gateway> findAllByOrganisationId(Long organisationId) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
