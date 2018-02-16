package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

public interface PhysicalMeters {

  Optional<PhysicalMeter> findById(Long id);

  List<PhysicalMeter> findByMedium(String medium);

  List<PhysicalMeter> findAll();

  PhysicalMeter save(PhysicalMeter physicalMeter);

  Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    Long organisationId,
    String externalId,
    String address
  );
}
