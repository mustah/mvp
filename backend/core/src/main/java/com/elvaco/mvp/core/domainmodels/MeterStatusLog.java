package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
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
    this.start = start;
    this.stop = stop;
  }

  MeterStatusLog withStop(ZonedDateTime stopTime) {
    return new MeterStatusLog(
      id,
      physicalMeterId,
      status,
      start,
      stopTime
    );
  }

  boolean isActive() {
    return stop == null;
  }
}
