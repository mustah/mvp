package com.elvaco.mvp.testing.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

import static java.util.stream.Collectors.toList;

public class MockLogicalMetersWithCascading extends MockLogicalMeters {

  private final PhysicalMeters physicalMeters;

  public MockLogicalMetersWithCascading(PhysicalMeters physicalMeters) {
    super();
    this.physicalMeters = physicalMeters;
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    List<PhysicalMeter> savedPhysicalMeters = logicalMeter.physicalMeters.stream()
      .map(physicalMeters::save)
      .collect(toList());

    return super.save(LogicalMeter.builder()
      .id(logicalMeter.id)
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .meterDefinition(logicalMeter.meterDefinition)
      .created(logicalMeter.created)
      .physicalMeters(savedPhysicalMeters)
      .location(logicalMeter.location)
      .alarms(logicalMeter.alarms)
      .build());
  }
}
