package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public final class FilterUtils {

  static List<UUID> toUuids(List<String> values) {
    return values.stream()
      .map(UUID::fromString)
      .collect(toList());
  }

  static ZonedDateTime getZonedDateTimeFrom(List<String> values) {
    return ZonedDateTime.parse(values.get(0));
  }

  public static boolean isYes(String v) {
    return "yes".equalsIgnoreCase(v);
  }
}
