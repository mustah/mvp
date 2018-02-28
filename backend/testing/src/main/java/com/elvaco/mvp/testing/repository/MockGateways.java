package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;

public class MockGateways extends MockRepository<Gateway> implements Gateways {

  @Override
  public List<Gateway> findAll() {
    return allMocks();
  }

  @Override
  public List<Gateway> findAllByOrganisationId(Long organisationId) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Gateway save(Gateway gateway) {
    return saveMock(gateway);
  }

  @Override
  protected Optional<Long> getId(Gateway entity) {
    return filter(gateway -> Objects.equals(gateway.id, entity.id))
      .map(gateway -> gateway.id)
      .findFirst();
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
}
