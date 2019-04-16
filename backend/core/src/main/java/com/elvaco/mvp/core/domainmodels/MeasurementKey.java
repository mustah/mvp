package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(exclude = "activePeriod")
public class MeasurementKey {

  public final UUID logicalMeterId;
  public final String utcOffset;
  public final String physicalMeterAddress;
  public final PeriodRange activePeriod;
  public final String quantity;
  public String externalId;
  public String city;
  public String locationAddress;
  public String mediumName;
}
