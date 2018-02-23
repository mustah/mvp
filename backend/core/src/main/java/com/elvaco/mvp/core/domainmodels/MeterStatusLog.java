package com.elvaco.mvp.core.domainmodels;

import java.util.Date;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MeterStatusLog {

  @Nullable
  public final Long id;
  public final long physicalMeterId;
  public final String name;
  public final Date start;
  @Nullable
  public final Date stop;
  public final long statusId;

  public MeterStatusLog(Long physicalMeterId, Long statusId, String name, Date start) {
    this(null, physicalMeterId, statusId, name, start, null);
  }

  public MeterStatusLog(
    @Nullable Long id,
    long physicalMeterId,
    long statusId,
    String name,
    Date start,
    @Nullable Date stop
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.name = name;
    this.start = new Date(start.getTime());
    this.stop = stop != null ? new Date(stop.getTime()) : null;
    this.statusId = statusId;
  }
}
