package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

public interface PhysicalMeters {

  Optional<PhysicalMeter> findById(Long id);

  Optional<PhysicalMeter> findByIdentity(String identity);

  List<PhysicalMeter> findByMedium(String medium);

  List<PhysicalMeter> findAll();

  PhysicalMeter save(PhysicalMeter physicalMeter);
}
