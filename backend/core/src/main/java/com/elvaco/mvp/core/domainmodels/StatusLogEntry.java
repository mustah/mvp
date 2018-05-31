package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.Dates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@AllArgsConstructor
@ToString
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

  static <T> StatusLogEntry<T> unknownFor(Identifiable<T> entity) {
    return new StatusLogEntry<>(
      entity.getId(),
      StatusType.UNKNOWN,
      Dates.epoch()
    );
  }

  public boolean isActive() {
    return stop == null;
  }

  public StatusLogEntry<T> withStop(ZonedDateTime stopTime) {
    return new StatusLogEntry<>(
      id,
      entityId,
      status,
      start,
      stopTime
    );
  }

  @Nullable
  @Override
  public Long getId() {
    return id;
  }
}
