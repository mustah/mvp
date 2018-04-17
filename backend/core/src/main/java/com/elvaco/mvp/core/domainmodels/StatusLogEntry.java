package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class StatusLogEntry<T> implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final T entityId;
  public final StatusType status;
  public final ZonedDateTime start;
  @Nullable
  public final ZonedDateTime stop;

  public StatusLogEntry(T entityId, StatusType status, ZonedDateTime start) {
    this(null, entityId, status, start, null);
  }

  public boolean isActive() {
    return stop == null;
  }

  public StatusLogEntry<T> withStop(ZonedDateTime stopTime) {
    return new StatusLogEntry<T>(
      id,
      entityId,
      status,
      start,
      stopTime
    );
  }

  @Override
  public Long getId() {
    return id;
  }
}
