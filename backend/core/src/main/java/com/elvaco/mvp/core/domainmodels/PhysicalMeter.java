package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@Builder
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class PhysicalMeter implements Identifiable<UUID> {

  @Default
  public UUID id = randomUUID();
  public final Organisation organisation;
  public final String address;
  public final String externalId;
  public final String medium;
  @Nullable
  public final String manufacturer;
  @Nullable
  public final UUID logicalMeterId;
  public final long readIntervalMinutes;
  public final Long measurementCount;
  @Default
  public List<StatusLogEntry<UUID>> statuses = new ArrayList<>();

  @Override
  public UUID getId() {
    return id;
  }

  public long getMeasurementCountOrZero() {
    return measurementCount != null ? measurementCount : 0;
  }

  public PhysicalMeter withMedium(String medium) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes,
      measurementCount,
      statuses
    );
  }

  public PhysicalMeter withManufacturer(String manufacturer) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes,
      measurementCount,
      statuses
    );
  }

  public PhysicalMeter withLogicalMeterId(UUID logicalMeterId) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes,
      measurementCount,
      statuses
    );
  }

  public PhysicalMeter withReadIntervalMinutes(@Nullable Long readIntervalMinutes) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes != null ? readIntervalMinutes : this.readIntervalMinutes,
      measurementCount,
      statuses
    );
  }

  public PhysicalMeter replaceActiveStatus(StatusType status) {
    return replaceActiveStatus(status, ZonedDateTime.now());
  }

  PhysicalMeter replaceActiveStatus(StatusType status, ZonedDateTime when) {
    List<StatusLogEntry<UUID>> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      statuses,
      new StatusLogEntry<>(
        id,
        status,
        when
      )
    );

    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes,
      measurementCount,
      unmodifiableList(newStatuses)
    );
  }
}
