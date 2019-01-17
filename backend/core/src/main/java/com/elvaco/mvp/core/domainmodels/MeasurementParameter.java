package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class MeasurementParameter {

  private final List<UUID> logicalMeterIds;
  private final List<Quantity> quantities;
  private final ZonedDateTime from;
  private final ZonedDateTime to;
  private final TemporalResolution resolution;
}
