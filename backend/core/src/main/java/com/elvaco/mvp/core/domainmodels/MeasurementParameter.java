package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeasurementParameter {

  private final List<UUID> physicalMeterIds;
  private final Quantity quantity;
  private final ZonedDateTime from;
  private final ZonedDateTime to;
  private final TemporalResolution resolution;
}
