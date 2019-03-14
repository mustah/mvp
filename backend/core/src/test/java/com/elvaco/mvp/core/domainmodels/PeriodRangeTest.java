package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class PeriodRangeTest {

  private static final ZonedDateTime ZDT_MAX = Instant.ofEpochMilli(Long.MAX_VALUE)
    .atZone(ZoneOffset.UTC);

  private static final ZonedDateTime ZDT_MIN = Instant.ofEpochMilli(Long.MIN_VALUE)
    .atZone(ZoneOffset.UTC);

  @Test
  public void stopBeforeStartThrowsException() {
    ZonedDateTime eight = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    ZonedDateTime nine = ZonedDateTime.parse("2018-01-01T09:00:00Z");
    assertThatThrownBy(() -> PeriodRange.halfOpenFrom(nine, eight)).isInstanceOf(
      IllegalArgumentException.class
    );

    PeriodRange validRange = PeriodRange.halfOpenFrom(eight, nine);
    assertThatThrownBy(() -> validRange.toBuilder()
      .start(PeriodBound.inclusiveOf(nine))
      .stop(PeriodBound.exclusiveOf(eight))
      .build()).isInstanceOf(
      IllegalArgumentException.class);
  }

  @Test
  public void halfOpenRangeContainsStart() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    PeriodRange periodRange = PeriodRange.halfOpenFrom(dateTime, null);
    assertThat(periodRange.contains(dateTime)).isTrue();
  }

  @Test
  public void halfOpenBoundedRangeDoesNotContainStop() {
    ZonedDateTime start = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    ZonedDateTime stop = ZonedDateTime.parse("2018-01-01T09:00:00Z");
    PeriodRange periodRange = PeriodRange.halfOpenFrom(start, stop);
    assertThat(periodRange.contains(stop)).isFalse();
    assertThat(periodRange.contains(stop.minusNanos(1))).isTrue();
  }

  @Test
  public void rangeDoesNotContainAnythingBeforeStart() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    PeriodRange periodRange = PeriodRange.halfOpenFrom(dateTime, null);
    assertThat(periodRange.contains(dateTime.minusNanos(1))).isFalse();
    assertThat(periodRange.contains(ZDT_MIN)).isFalse();
  }

  @Test
  public void rightUnboundedRangeContainsEverythingLaterThanStart() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    PeriodRange periodRange = PeriodRange.halfOpenFrom(dateTime, null);
    assertThat(periodRange.contains(ZDT_MAX)).isTrue();
  }

  @Test
  public void emptyRangeIsEmpty() {
    assertThat(PeriodRange.empty().isEmpty()).isTrue();
  }

  @Test
  public void emptyRangeContainsNothing() {
    ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    assertThat(PeriodRange.empty().contains(dateTime)).isFalse();
  }

  @Test
  public void rightOpen_whenEmpty() {
    assertThat(PeriodRange.empty().isRightOpen()).isTrue();
  }

  @Test
  public void rightOpen_whenStartIsSet() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    assertThat(PeriodRange.halfOpenFrom(dateTime, null).isRightOpen()).isTrue();
  }

  @Test
  public void rightOpen_whenStartAndStopIsSet() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    assertThat(PeriodRange.halfOpenFrom(dateTime, dateTime.plusDays(27)).isRightOpen()).isFalse();
  }

  @Test
  public void rightOpen_whenStopIsSet() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    assertThat(PeriodRange.halfOpenFrom(null, dateTime).isRightOpen()).isFalse();
  }

  @Test
  public void closedBoundsOverInfinityIsConvertedToOpen() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-01T08:00:00Z");
    assertThat(PeriodRange.closedFrom(null, dateTime)).isEqualTo(
      new PeriodRange(PeriodBound.exclusiveOf(null), PeriodBound.inclusiveOf(dateTime))
    );

    assertThat(PeriodRange.closedFrom(dateTime, null)).isEqualTo(
      new PeriodRange(PeriodBound.inclusiveOf(dateTime), PeriodBound.exclusiveOf(null))
    );

    assertThat(PeriodRange.closedFrom(null, null)).isEqualTo(
      new PeriodRange(PeriodBound.exclusiveOf(null), PeriodBound.exclusiveOf(null))
    );
  }
}
