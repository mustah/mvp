package com.elvaco.mvp.core.dto;

import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GatewaySummaryDto implements Identifiable<UUID> {
  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final Set<StatusLogEntry<UUID>> statusLogs;
  public final Set<LogicalMeterLocation> meters;

  public Location getLocation() {
    // FIXME: This doesn't really make sense once we have >1 meter
    return meters.stream()
      .findFirst()
      .map(meterDto -> meterDto.location)
      .orElse(Location.UNKNOWN_LOCATION);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
