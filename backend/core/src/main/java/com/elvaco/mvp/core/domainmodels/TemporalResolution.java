package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;

import lombok.ToString;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

@ToString
public enum TemporalResolution implements StartInterval {

  hour(HOURS) {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(HOURS).toInstant(),
        zonedDateTime.getZone()
      );
    }
  },

  day(DAYS) {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(DAYS).toInstant(),
        zonedDateTime.getZone()
      );
    }
  },

  month(MONTHS) {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(DAYS).with(firstDayOfMonth()).toInstant(),
        zonedDateTime.getZone()
      );
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

  public TemporalUnit getTemporalUnit() {
    return this.unit;
  }

}

