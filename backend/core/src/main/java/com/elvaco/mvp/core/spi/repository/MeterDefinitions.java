package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;

public interface MeterDefinitions {
  MeterDefinition save(MeterDefinition meterDefinition);

  List<MeterDefinition> findAll();
}
