package com.elvaco.mvp.consumers.rabbitmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

class MockPhysicalMeters implements com.elvaco.mvp.core.spi.repository.PhysicalMeters {
  private List<PhysicalMeter> physicalMeters;

  MockPhysicalMeters() {
    this.physicalMeters = new ArrayList<>();
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
    return physicalMeters;
  }

  @Override
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    if (physicalMeter.id != null) {
      physicalMeters.set(Math.toIntExact(physicalMeter.id), physicalMeter);
    } else {
      physicalMeter = new PhysicalMeter(
        (long) physicalMeters.size(),
        physicalMeter.organisation,
        physicalMeter.address,
        physicalMeter.externalId,
        physicalMeter.medium,
        physicalMeter.manufacturer
      );
      physicalMeters.add(physicalMeter);
    }
    return physicalMeter;
  }

  @Override
  public Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    Long organisationId, String externalId, String identity
  ) {
    return physicalMeters.stream()
      .filter(physicalMeter -> physicalMeter.organisation.id.equals(organisationId))
      .filter(physicalMeter -> physicalMeter.externalId.equals(externalId))
      .filter(physicalMeter -> physicalMeter.address.equals(identity))
      .findFirst();
  }
}
