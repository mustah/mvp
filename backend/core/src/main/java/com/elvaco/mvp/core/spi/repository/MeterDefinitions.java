package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;

public interface MeterDefinitions {
  MeterDefinition save(MeterDefinition meterDefinition);

  Optional<MeterDefinition> findById(Long id);

  List<MeterDefinition> findAll();
}
