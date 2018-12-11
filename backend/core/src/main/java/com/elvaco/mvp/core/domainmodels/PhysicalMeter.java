package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.UUID.randomUUID;

@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhysicalMeter implements Identifiable<UUID>, PrimaryKeyed {

  public final UUID organisationId;
  public final String address;
  public final String externalId;
  public final String medium;
  @Nullable
  public final String manufacturer;
  @Nullable
  public final UUID logicalMeterId;
  @Default
  public UUID id = randomUUID();
  public long readIntervalMinutes;
  public Integer revision;
  @Nullable
  public Integer mbusDeviceType;
  @Singular
  public List<StatusLogEntry> statuses;
  @Singular
  public List<AlarmLogEntry> alarms;

  @Override
  public UUID getId() {
    return id;
  }

  public PhysicalMeter replaceActiveStatus(StatusType status, ZonedDateTime when) {
    List<StatusLogEntry> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      statuses,
      StatusLogEntry.builder()
        .primaryKey(primaryKey())
        .status(status)
        .start(when)
        .build()
    );
    this.statuses = new ArrayList<>();
    return toBuilder().statuses(newStatuses).build();
  }

  @Override
  public PrimaryKey primaryKey() {
    return new Pk(id, organisationId);
  }
}
