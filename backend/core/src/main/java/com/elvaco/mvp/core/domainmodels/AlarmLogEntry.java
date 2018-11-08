package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class AlarmLogEntry implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final UUID entityId;
  public final ZonedDateTime start;
  public final ZonedDateTime lastSeen;
  @Nullable
  public final ZonedDateTime stop;
  public final int mask;
  @Nullable
  public final String description;

  @Nullable
  @Override
  public Long getId() {
    return id;
  }

  public boolean isActive() {
    return stop == null;
  }
}
