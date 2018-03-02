package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

public interface PhysicalMeters {

  List<PhysicalMeter> findByMedium(String medium);

  List<PhysicalMeter> findAll();

  PhysicalMeter save(PhysicalMeter physicalMeter);

  Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  );
}
