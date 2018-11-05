package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.Dates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class StatusLogEntry<T> implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final T entityId;
  public final StatusType status;
  @Builder.Default
  public ZonedDateTime start = ZonedDateTime.now();
  @Nullable
  public final ZonedDateTime stop;

  public static <T> StatusLogEntry<T> unknownFor(Identifiable<T> entity) {
    return StatusLogEntry.<T>builder()
      .entityId(entity.getId())
      .status(StatusType.UNKNOWN)
      .start(Dates.epoch())
      .build();
  }

  public boolean isActive() {
    return stop == null;
  }

  @Nullable
  @Override
  public Long getId() {
    return id;
  }
}
