package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface PhysicalMeters {

  List<PhysicalMeter> findByMedium(String medium);

  List<PhysicalMeter> findAll();

  Page<PhysicalMeter> findAll(RequestParameters parameters, Pageable pageable);

  PhysicalMeter save(PhysicalMeter physicalMeter);

  Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  );
}
