package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

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
  @Default
  public PeriodRange activePeriod = PeriodRange.empty();

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public PrimaryKey primaryKey() {
    return new Pk(id, organisationId);
  }

  public boolean isActive(ZonedDateTime when) {
    return activePeriod.contains(when);
  }

  public PhysicalMeter setStatuses(List<StatusLogEntry> statuses) {
    this.statuses = statuses;
    return this;
  }

  public PhysicalMeter deactivate(ZonedDateTime dateTime) {
    this.activePeriod = activePeriod.toBuilder().stop(PeriodBound.exclusiveOf(dateTime)).build();
    return this;
  }

  public PhysicalMeter activate(ZonedDateTime dateTime) {
    this.activePeriod = activePeriod.toBuilder().start(PeriodBound.inclusiveOf(dateTime)).build();
    return this;
  }
}
