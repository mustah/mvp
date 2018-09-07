package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

public class MockPhysicalMeters extends MockRepository<UUID, PhysicalMeter>
  implements PhysicalMeters {

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return emptyList();
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return allMocks();
  }

  @Override
  public Page<PhysicalMeter> findAll(
    RequestParameters parameters, Pageable pageable
  ) {
    throw new NotImplementedYet();
  }

  @Override
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    return saveMock(physicalMeter);
  }

  @Override
  public Optional<PhysicalMeter> findByWithStatuses(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return filter(physicalMeter -> physicalMeter.organisation.id.equals(organisationId))
      .filter(physicalMeter -> physicalMeter.externalId.equals(externalId))
      .filter(physicalMeter -> physicalMeter.address.equals(address))
      .findFirst();
  }

  @Override
  public Optional<PhysicalMeter> findByWithAlarms(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return findByWithStatuses(organisationId, externalId, address);
  }

  @Override
  public Optional<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return findByWithStatuses(organisationId, externalId, address);
  }

  @Override
  protected PhysicalMeter copyWithId(UUID id, PhysicalMeter entity) {
    return PhysicalMeter.builder()
      .id(id)
      .organisation(entity.organisation)
      .address(entity.address)
      .externalId(entity.externalId)
      .medium(entity.medium)
      .manufacturer(entity.manufacturer)
      .logicalMeterId(entity.logicalMeterId)
      .readIntervalMinutes(entity.readIntervalMinutes)
      .build();
  }

  @Override
  protected UUID generateId(PhysicalMeter entity) {
    return randomUUID();
  }
}
