package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;

public interface MeterDefinitions {
  MeterDefinition save(MeterDefinition meterDefinition);

  Optional<MeterDefinition> findById(Long id);

  Optional<MeterDefinition> findSystemMeterDefinition(Medium medium);

  List<MeterDefinition> findAll(UUID organisationId);

  List<MeterDefinition> findAll();

  void deleteById(Long id);
}
