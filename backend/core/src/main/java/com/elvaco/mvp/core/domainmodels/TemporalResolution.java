package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public enum TemporalResolution implements StartInterval {

  hour() {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(HOURS).toInstant(),
        zonedDateTime.getZone()
      );
    }
  },

  day {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(DAYS).toInstant(),
        zonedDateTime.getZone()
      );
    }
  },

  month {
    @Override
    public OffsetDateTime getStart(ZonedDateTime zonedDateTime) {
      return OffsetDateTime.ofInstant(
        zonedDateTime.truncatedTo(DAYS).with(firstDayOfMonth()).toInstant(),
        zonedDateTime.getZone()
      );
    }
  };

  private static final Map<String, TemporalResolution> STRING_TO_ENUM = Stream.of(values())
    .collect(toMap(Object::toString, identity()));

  public static TemporalResolution defaultResolutionFor(Duration duration) {
    if (duration.toDays() < 2) {
      return hour;
    } else if (duration.toDays() < 60) {
      return day;
    } else {
      return month;
    }
  }

  public static Optional<TemporalResolution> fromString(String resolution) {
    return Optional.ofNullable(STRING_TO_ENUM.get(resolution));
  }

  public String asInterval() {
    return "1 " + this.name();
  }
}

