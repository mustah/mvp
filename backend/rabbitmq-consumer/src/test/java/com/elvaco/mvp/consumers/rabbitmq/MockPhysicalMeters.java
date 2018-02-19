package com.elvaco.mvp.consumers.rabbitmq;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

class MockPhysicalMeters extends MockRepository<PhysicalMeter> implements com.elvaco.mvp.core.spi
  .repository.PhysicalMeters {

  @Override
  Optional<Long> getId(PhysicalMeter entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  PhysicalMeter copyWithId(Long id, PhysicalMeter entity) {
    return new PhysicalMeter(
      id,
      entity.organisation,
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer
    );
  }

  @Override
  public Optional<PhysicalMeter> findById(Long id) {
    return Optional.empty();
  }

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return null;
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
    Long organisationId, String externalId, String identity
  ) {
    return filter(physicalMeter -> physicalMeter.organisation.id.equals(organisationId))
      .filter(physicalMeter -> physicalMeter.externalId.equals(externalId))
      .filter(physicalMeter -> physicalMeter.address.equals(identity))
      .findFirst();
  }
}
