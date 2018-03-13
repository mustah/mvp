package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeterStatusLog {

  @Nullable
  public final Long id;
  public final UUID physicalMeterId;
  public final long statusId;
  public final String name;

  //TODO ZonedDateTime
  public final Date start;
  @Nullable
  public final Date stop;

  //TODO remove
  public MeterStatusLog(
    @Nullable Long id,
    UUID physicalMeterId,
    long statusId,
    String name,
    Date start,
    @Nullable Date stop
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.statusId = statusId;
    this.name = name;
    this.start = new Date(start.getTime());
    this.stop = stop != null ? new Date(stop.getTime()) : null;
  }

  public MeterStatusLog(
    @Nullable Long id,
    UUID physicalMeterId,
    long statusId,
    String name,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.statusId = statusId;
    this.name = name;
    this.start = Date.from(start.toInstant());
    this.stop = stop != null ? Date.from(stop.toInstant()) : null;
  }
}
