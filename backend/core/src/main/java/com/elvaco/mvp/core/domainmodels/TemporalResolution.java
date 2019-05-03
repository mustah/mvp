package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

import lombok.ToString;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

@ToString
public enum TemporalResolution implements StartInterval, TemporalUnit {

  all(MILLIS) {
    @Override
    public ZonedDateTime getStart(ZonedDateTime zonedDateTime) {
      return zonedDateTime;
    }
  },

  hour(HOURS) {
    @Override
    public ZonedDateTime getStart(ZonedDateTime zonedDateTime) {
      return zonedDateTime.truncatedTo(HOURS);
    }
  },

  day(DAYS) {
    @Override
    public ZonedDateTime getStart(ZonedDateTime zonedDateTime) {
      return zonedDateTime.truncatedTo(DAYS);
    }
  },

  month(MONTHS) {
    @Override
    public ZonedDateTime getStart(ZonedDateTime zonedDateTime) {
      return zonedDateTime.with(firstDayOfMonth()).truncatedTo(DAYS);
    }
  };

  private final TemporalUnit unit;

  TemporalResolution(TemporalUnit unit) {
    this.unit = unit;
  }

  public static TemporalResolution defaultResolutionFor(Duration duration) {
    if (duration.toDays() < 2) {
      return hour;
    } else if (duration.toDays() < 60) {
      return day;
    } else {
      return month;
    }
  }

  @Override
  public Duration getDuration() {
    return unit.getDuration();
  }

  @Override
  public boolean isDurationEstimated() {
    return unit.isDurationEstimated();
  }

  @Override
  public boolean isDateBased() {
    return unit.isDateBased();
  }

  @Override
  public boolean isTimeBased() {
    return unit.isTimeBased();
  }

  @Override
  public <R extends Temporal> R addTo(R r, long l) {
    return unit.addTo(r, l);
  }

  @Override
  public long between(Temporal temporal, Temporal temporal1) {
    return unit.between(temporal, temporal1);
  }
}

