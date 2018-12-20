package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class PeriodRange implements Serializable {

  private static final long serialVersionUID = -5369174948443667940L;
  private static final PeriodRange EMPTY = new PeriodRange();
  public final PeriodBound start;
  public final PeriodBound stop;

  private PeriodRange() {
    start = PeriodBound.exclusiveOf(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
    stop = PeriodBound.exclusiveOf(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
  }

  public PeriodRange(PeriodBound start, PeriodBound stop) {
    validateBounds(start, stop);
    this.start = start;
    this.stop = stop;
  }

  /**
   * This is always left-closed, right-open, since that's the most common use.
   */
  public static PeriodRange halfOpenFrom(
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    return new PeriodRange(
      PeriodBound.inclusiveOf(start),
      PeriodBound.exclusiveOf(stop)
    );
  }

  public static PeriodRange openFrom(@Nullable ZonedDateTime start, @Nullable ZonedDateTime stop) {
    return new PeriodRange(
      PeriodBound.exclusiveOf(start),
      PeriodBound.exclusiveOf(stop)
    );
  }

  public static PeriodRange closedFrom(
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    return new PeriodRange(
      PeriodBound.inclusiveOf(start),
      PeriodBound.inclusiveOf(stop)
    );
  }

  public static PeriodRange from(PeriodBound start) {
    return new PeriodRange(
      start,
      PeriodBound.unboundedExclusive()
    );
  }

  public static PeriodRange empty() {
    return EMPTY;
  }

  public static PeriodRange unbounded() {
    return halfOpenFrom(null, null);
  }

  public boolean isEmpty() {
    return this == EMPTY;
  }

  public Optional<ZonedDateTime> getStartDateTime() {
    return Optional.ofNullable(start.dateTime);
  }

  public Optional<ZonedDateTime> getStopDateTime() {
    return Optional.ofNullable(stop.dateTime);
  }

  public boolean contains(ZonedDateTime when) {
    if (isEmpty()) {
      return false;
    }
    Function<ZonedDateTime, Boolean> startIncludedFunc;
    if (start.isInclusive) {
      startIncludedFunc = startTime -> startTime.isEqual(when) || startTime.isBefore(when);
    } else {
      startIncludedFunc = startTime -> startTime.isBefore(when);
    }

    Function<ZonedDateTime, Boolean> stopIncludedFunc;
    if (stop.isInclusive) {
      stopIncludedFunc = stopTime -> stopTime.isEqual(when) || stopTime.isAfter(when);
    } else {
      stopIncludedFunc = stopTime -> stopTime.isAfter(when);
    }
    return getStartDateTime().map(startIncludedFunc).orElse(true)
      && getStopDateTime().map(stopIncludedFunc).orElse(true);
  }

  public boolean isRightOpen() {
    return this == EMPTY || !this.getStopDateTime().isPresent();
  }

  private void validateBounds(PeriodBound start, PeriodBound stop) {
    if (start.dateTime == null || stop.dateTime == null) {
      return;
    }

    if (start.dateTime.isAfter(stop.dateTime)) {
      throw new IllegalArgumentException(String.format(
        "Stop time can not be before start time (start = '%s', stop = '%s')",
        start.dateTime,
        stop.dateTime
      ));
    }
  }
}
