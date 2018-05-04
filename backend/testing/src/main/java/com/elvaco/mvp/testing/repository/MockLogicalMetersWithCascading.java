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
      .map(this.physicalMeters::save)
      .collect(toList());

    return super.save(new LogicalMeter(
      logicalMeter.id,
      logicalMeter.externalId,
      logicalMeter.organisationId,
      logicalMeter.location,
      logicalMeter.created,
      savedPhysicalMeters,
      logicalMeter.meterDefinition,
      logicalMeter.gateways,
      logicalMeter.collectionPercentage,
      logicalMeter.measurements
    ));
  }
}