package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.Pk;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogicalMeterSummaryDto {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final ZonedDateTime created;
  public final Location location;
  public final String medium;
  public final String gatewaySerial;
  @Nullable
  public final Double collectionPercentage;
  @Nullable
  public final AlarmLogEntry activeAlarm;
  @Nullable
  public final String status;
  @Nullable
  public final String manufacturer;
  @Nullable
  public final String address;
  @Nullable
  public final Long readIntervalMinutes;
  @Nullable
  public Long expectedReadingCount;

  public LogicalMeterSummaryDto(
    UUID id,
    UUID organisationId,
    String externalId,
    OffsetDateTime created,
    String medium,
    String gatewaySerial,
    @Nullable Double collectionPercentage,
    @Nullable String status,
    @Nullable String manufacturer,
    @Nullable String address,
    @Nullable Long readIntervalMinutes,
    Double latitude,
    Double longitude,
    Double confidence,
    String country,
    String city,
    String streetAddress,
    @Nullable String zip,
    @Nullable Long alarmId,
    @Nullable UUID alarmPhysicalMeterId,
    @Nullable OffsetDateTime start,
    @Nullable OffsetDateTime lastSeen,
    @Nullable OffsetDateTime stop,
    @Nullable Integer mask
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.created = created.toZonedDateTime();
    this.medium = medium;
    this.gatewaySerial = gatewaySerial;
    this.status = status;
    this.collectionPercentage = collectionPercentage;
    this.manufacturer = manufacturer;
    this.address = address;
    this.readIntervalMinutes = readIntervalMinutes;
    this.location = new Location(
      latitude,
      longitude,
      confidence,
      country,
      city,
      streetAddress,
      zip
    );
    this.activeAlarm = alarmId != null
      ? AlarmLogEntry.builder()
      .id(alarmId)
      .primaryKey(new Pk(alarmPhysicalMeterId, organisationId))
      .start(start.toZonedDateTime())
      .lastSeen(lastSeen.toZonedDateTime())
      .stop(stop != null ? stop.toZonedDateTime() : null)
      .mask(mask)
      .build()
      : null;
  }
}
