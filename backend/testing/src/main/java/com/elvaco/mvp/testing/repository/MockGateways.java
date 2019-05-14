package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

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
  public Optional<Gateway> findBy(
    UUID organisationId,
    String productModel, String serial
  ) {
    return filter(gateway -> gateway.serial.equals(serial))
      .filter(gateway -> gateway.organisationId.equals(organisationId))
      .filter(gateway -> gateway.productModel.equals(productModel))
      .findFirst();
  }

  @Override
  public Optional<Gateway> findBy(UUID organisationId, String serial) {
    return filter(gateway -> gateway.serial.equals(serial))
      .filter(gateway -> gateway.organisationId.equals(organisationId))
      .findFirst();
  }

  @Override
  public List<Gateway> findBy(String serial) {
    return filter(gateway -> gateway.serial.equals(serial)).collect(toList());
  }

  @Override
  public Optional<Gateway> findById(UUID id) {
    return Optional.empty();
  }

  @Override
  public Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return Optional.empty();
  }

  @Override
  public Page<String> findSerials(
    RequestParameters parameters, Pageable pageable
  ) {
    throw new NotImplementedYet();
  }

  @Override
  protected Gateway copyWithId(UUID id, Gateway entity) {
    return Gateway.builder()
      .id(id)
      .organisationId(entity.organisationId)
      .serial(entity.serial)
      .productModel(entity.productModel)
      .meters(entity.meters)
      .statusLogs(entity.statusLogs)
      .build();
  }

  @Override
  protected UUID generateId(Gateway entity) {
    return randomUUID();
  }
}
