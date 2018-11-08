package com.elvaco.mvp.core.dto;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.StatusType;
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
  public final Long missingReadingCount;
  @Nullable
  public final AlarmLogEntry activeAlarm;
  @Nullable
  public final StatusType activeStatus;
  @Nullable
  public final String manufacturer;
  @Nullable
  public final String address;
  @Nullable
  public final Long readIntervalMinutes;
  @Nullable
  public Long expectedReadingCount;
}
