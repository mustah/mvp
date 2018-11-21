package com.elvaco.mvp.core.domainmodels;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public enum TemporalResolution {
  hour,
  day,
  month;

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
}

