package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.PrimaryKeyed;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;

public class GatewaySummaryDto implements Identifiable<UUID>, PrimaryKeyed {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  @Nullable
  public final StatusLogEntry statusLog;
  public final Set<LogicalMeterLocation> meterLocations;

  public GatewaySummaryDto(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    @Nullable Long statusLogId,
    @Nullable StatusType statusType,
    @Nullable OffsetDateTime statusStart,
    @Nullable OffsetDateTime statusStop
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.statusLog = statusLogId != null
      ?
      StatusLogEntry.builder()
        .id(statusLogId)
        .primaryKey(new Pk(id, organisationId))
        .status(statusType)
        .start(Optional.ofNullable(statusStart).map(OffsetDateTime::toZonedDateTime).orElse(null))
        .stop(Optional.ofNullable(statusStop).map(OffsetDateTime::toZonedDateTime).orElse(null))
        .build()
      : null;

    this.meterLocations = new HashSet<>();
  }

  public void addLocation(LogicalMeterLocation location) {
    this.meterLocations.add(location);
  }

  public Location getLocation() {
    // FIXME: This doesn't really make sense once we have >1 meter
    return meterLocations.stream()
      .findFirst()
      .map(meterDto -> meterDto.location)
      .orElse(Location.UNKNOWN_LOCATION);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public PrimaryKey primaryKey() {
    return new Pk(id, organisationId);
  }
}
