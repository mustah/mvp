package com.elvaco.mvp.core.domainmodels;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class MeterSummary {

  public final long meters;
  public final long cities;
  public final long addresses;
}
