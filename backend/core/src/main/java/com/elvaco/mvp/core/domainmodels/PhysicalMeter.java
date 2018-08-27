package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@Builder
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class PhysicalMeter implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = -7089862561226980327L;

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
  @Singular
  public List<StatusLogEntry<UUID>> statuses;

  @Override
  public UUID getId() {
    return id;
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
      unmodifiableList(statuses)
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
      unmodifiableList(statuses)
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
      unmodifiableList(statuses)
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
      unmodifiableList(statuses)
    );
  }

  public PhysicalMeter replaceActiveStatus(StatusType status) {
    return replaceActiveStatus(status, ZonedDateTime.now());
  }

  PhysicalMeter replaceActiveStatus(StatusType status, ZonedDateTime when) {
    List<StatusLogEntry<UUID>> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      statuses,
      StatusLogEntry.<UUID>builder()
        .entityId(id)
        .status(status)
        .start(when)
        .build()
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
      unmodifiableList(newStatuses)
    );
  }
}
