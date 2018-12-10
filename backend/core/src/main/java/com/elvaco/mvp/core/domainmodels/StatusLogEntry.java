package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.util.Dates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusLogEntry implements Identifiable<Long>, PrimaryKeyed {

  @Nullable
  public final Long id;
  public final PrimaryKey primaryKey;
  public final StatusType status;
  @Builder.Default
  public ZonedDateTime start = ZonedDateTime.now();
  @Nullable
  public final ZonedDateTime stop;

  public static StatusLogEntry unknownFor(PrimaryKeyed item) {
    return StatusLogEntry.builder()
      .primaryKey(item.primaryKey())
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

  @Override
  public PrimaryKey primaryKey() {
    return primaryKey;
  }
}
