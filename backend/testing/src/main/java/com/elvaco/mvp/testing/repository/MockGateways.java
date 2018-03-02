package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;

import static java.util.UUID.randomUUID;

public class MockGateways extends MockRepository<UUID, Gateway> implements Gateways {

  @Override
  public List<Gateway> findAll() {
    return allMocks();
  }

  @Override
  public Gateway save(Gateway gateway) {
    return saveMock(gateway);
  }

  @Override
  protected Gateway copyWithId(UUID id, Gateway entity) {
    return new Gateway(
      id,
      entity.organisationId,
      entity.serial,
      entity.productModel,
      entity.meters
    );
  }

  @Override
  protected UUID generateId() {
    return randomUUID();
  }

  @Override
  public List<Gateway> findAllByOrganisationId(UUID organisationId) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
