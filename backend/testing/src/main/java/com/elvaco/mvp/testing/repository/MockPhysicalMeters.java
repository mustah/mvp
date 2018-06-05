package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

public class MockPhysicalMeters extends MockRepository<UUID, PhysicalMeter>
  implements PhysicalMeters {

  @Override
  protected PhysicalMeter copyWithId(UUID id, PhysicalMeter entity) {
    return new PhysicalMeter(
      id,
      entity.organisation,
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readIntervalMinutes,
      emptyList()
    );
  }

  @Override
  protected UUID generateId() {
    return randomUUID();
  }

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return emptyList();
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return allMocks();
  }

  @Override
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    return saveMock(physicalMeter);
  }

  @Override
  public Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String identity
  ) {
    return filter(physicalMeter -> physicalMeter.organisation.id.equals(organisationId))
      .filter(physicalMeter -> physicalMeter.externalId.equals(externalId))
      .filter(physicalMeter -> physicalMeter.address.equals(identity))
      .findFirst();
  }
}
