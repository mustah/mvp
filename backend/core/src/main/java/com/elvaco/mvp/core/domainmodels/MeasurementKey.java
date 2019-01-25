package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class MeasurementKey {

  public final UUID logicalMeterId;
  public final String physicalMeterAddress;
  public final String quantity;
}
