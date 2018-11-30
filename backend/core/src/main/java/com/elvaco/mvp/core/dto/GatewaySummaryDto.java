package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class GatewaySummaryDto implements Identifiable<UUID> {
  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  @Nullable
  public final StatusLogEntry<UUID> statusLog;
  public final Set<LogicalMeterLocation> meterLocations;

  public GatewaySummaryDto(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    @Nullable Long statusLogId,
    @Nullable StatusType statusType,
    @Nullable OffsetDateTime statusStart,
    @Nullable OffsetDateTime statusStop,
    @Nullable UUID logicalMeterId,
    @Nullable Double latitude,
    @Nullable Double longitude,
    @Nullable Double confidence,
    @Nullable String country,
    @Nullable String city,
    @Nullable String streetAddress
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.statusLog = statusLogId != null ? new StatusLogEntry<>(
      statusLogId,
      id,
      organisationId,
      statusType,
      Optional.ofNullable(statusStart).map(OffsetDateTime::toZonedDateTime).orElse(null),
      Optional.ofNullable(statusStop).map(OffsetDateTime::toZonedDateTime).orElse(null)
    ) : null;

    this.meterLocations = logicalMeterId != null ? singleton(
      new LogicalMeterLocation(
        logicalMeterId,
        new Location(latitude, longitude, confidence, country, city, streetAddress)
      )
    ) : emptySet();
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
}
