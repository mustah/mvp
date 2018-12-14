package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

public interface PhysicalMeters {

  List<PhysicalMeter> findByMedium(String medium);

  List<PhysicalMeter> findAll();

  PhysicalMeter save(PhysicalMeter physicalMeter);

  Optional<PhysicalMeter> findByWithStatuses(
    UUID organisationId,
    String externalId,
    String address
  );

  List<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId
  );

  Optional<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId,
    String address
  );
}
