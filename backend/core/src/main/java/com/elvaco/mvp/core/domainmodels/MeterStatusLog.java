package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeterStatusLog {

  @Nullable
  public final Long id;
  public final UUID physicalMeterId;
  public final StatusType status;
  public final ZonedDateTime start;
  @Nullable
  public final ZonedDateTime stop;

  public MeterStatusLog(
    @Nullable Long id,
    UUID physicalMeterId,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.status = status;
    this.start = ZonedDateTime.ofInstant(start.toInstant(), start.getZone());
    this.stop = stop != null ? ZonedDateTime.ofInstant(stop.toInstant(), stop.getZone()) : null;
  }
}
