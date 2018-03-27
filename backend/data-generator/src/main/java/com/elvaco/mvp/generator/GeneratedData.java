package com.elvaco.mvp.generator;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

public class GeneratedData {

  public final LogicalMeter logicalMeter;
  public final List<Measurement> measurements;
  public final PhysicalMeter physicalMeter;
  public final Gateway gateway;

  GeneratedData(
    LogicalMeter logicalMeter,
    List<Measurement> measurements,
    PhysicalMeter physicalMeter,
    Gateway gateway
  ) {
    this.logicalMeter = logicalMeter;
    this.measurements = measurements;
    this.physicalMeter = physicalMeter;
    this.gateway = gateway;
  }
}
