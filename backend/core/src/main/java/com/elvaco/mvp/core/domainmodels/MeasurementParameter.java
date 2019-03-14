package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.spi.data.RequestParameters;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class MeasurementParameter {

  private final RequestParameters parameters;
  private final List<QuantityParameter> quantities;
  private final ZonedDateTime from;
  private final ZonedDateTime to;
  private final TemporalResolution resolution;
}
