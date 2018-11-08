package com.elvaco.mvp.core.dto;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LogicalMeterLocation {
  public final UUID id;
  public final Location location;
}
